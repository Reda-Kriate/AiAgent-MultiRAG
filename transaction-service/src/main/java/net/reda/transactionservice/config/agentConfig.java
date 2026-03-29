package net.reda.transactionservice.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
public class agentConfig {

    @Bean
    ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer) {
        //stocke les 10 derniers messages de ai chat
        return chatId -> MessageWindowChatMemory.withMaxMessages(10);
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        //le modele de embedding
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
        //base de données pour stocker les embeddings
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5433)
                .database("agenticDb")
                .user("admin")
                .password("1234")
                .table("data_vs_v3")
                .dimension(embeddingModel.dimension())
                .dropTableFirst(false)
                .build();
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingModel model,
                                      EmbeddingStore<TextSegment> embeddingStore) {
        //récupérer les documents pertinents ou bien similaire à partir du vector store.
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(model)
                .embeddingStore(embeddingStore)
                .maxResults(10) //nombres maximum de documents à retourner
                .minScore(0.6) //score minimal de similarité pour qq'un document soit considéré pertinent
                .build();
    }



    @Bean
    ApplicationRunner loadDocumentToVectorStore(
            ChatLanguageModel chatLanguageModel,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            Tokenizer tokenizer,
            @Value("classpath:/docs/cv2.pdf") Resource pdfResource) {

        return args -> {
            //fait une recherche rapide pour voir si le vector store contient déjà au moins un embedding.
            var existing = embeddingStore.search(
                    dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                            .queryEmbedding(embeddingModel.embed("test").content())
                            .maxResults(1)
                            .build()
            );
            //si oui en saute l'ingestion
            if (!existing.matches().isEmpty()) {
                System.out.println("[agentConfig] Vector store already populated — skipping ingestion.");
                return;
            }

//            - 1000 → taille max d’un segment en tokens.
//            - 100 → chevauchement entre segments pour garder le contexte. (
//                    - si on prend par exemple la taille de segment c’est 1000, le deuxieme segemts commence a partir de 900 octet pour que le systeme sait chaque segment et leurs ordres
//                    - )
//            - tokenizer → utilisé pour compter les tokens correctement.
            var ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(1000, 100, tokenizer))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            loadDataIntoVectorStore(pdfResource, tokenizer, chatLanguageModel, ingestor);
        };
    }


        //1. Extraction du texte page par page.
        //2. Extraction des images et sauvegarde en PNG.
        //3. Envoi des images au modèle de langage pour générer une description textuelle.
        //4. Création de documents combinant texte + images pour ingestion dans le vector store.
    public void loadDataIntoVectorStore(Resource pdfResource,
                                        Tokenizer tokenizer,
                                        ChatLanguageModel chatLanguageModel,
                                        EmbeddingStoreIngestor embeddingStoreIngestor)
            throws IOException {

        //charger le pdf
        PDDocument document = PDDocument.load(pdfResource.getFile());
        //tous les pages
        PDPageTree pdPages = document.getPages();
        //permet extraire le texte page par page
        PDFTextStripper pdfTextStripper = new PDFTextStripper();

        int imageIndex = 0;
        int pageNumber = 0;

        List<Document> allImageDescriptionDocs = new ArrayList<>();

        //on lit chaque page individuellement
        for (PDPage pdPage : pdPages) {
            ++pageNumber;
            pdfTextStripper.setStartPage(pageNumber);
            pdfTextStripper.setEndPage(pageNumber);
            //textContent contient tous le texte de la page
            String textContent = pdfTextStripper.getText(document);

            PDResources resources = pdPage.getResources();

            List<String> imagePathsOnPage = new ArrayList<>();
            List<Document> pageImageDocs = new ArrayList<>();

            for (var xObjectName : resources.getXObjectNames()) {
                PDXObject pdxObject = resources.getXObject(xObjectName);

                if (!(pdxObject instanceof PDImageXObject image)) {
                    continue; // on ignore les non-images
                }

                ++imageIndex;
                String imagePath = "images/page_" + pageNumber + "_im_" + imageIndex + ".png";
                imagePathsOnPage.add(imagePath);

                BufferedImage bufferedImage = image.getImage();
                try (FileOutputStream fos = new FileOutputStream(imagePath)) {
                    ImageIO.write(bufferedImage, "png", fos);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                String imageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

                Image img = Image.builder()
                        .base64Data(imageBase64)
                        .mimeType("image/png")
                        .build();

                UserMessage userMessage = UserMessage.from(
                        TextContent.from("Describe this image in detail."),
                        ImageContent.from(img)
                );
                Response<AiMessage> response = chatLanguageModel.generate(userMessage);
                String imageDescription = response.content().text();
                System.out.println("[Page " + pageNumber + "] Image description: " + imageDescription);

                textContent = textContent
                        + "\nIMAGE: " + imagePath
                        + "\nDescription:\n" + imageDescription;

                Metadata imgMetadata = new Metadata();
                imgMetadata.put("page", pageNumber);
                imgMetadata.put("media", imagePath);
                imgMetadata.put("type", "image_description");
                pageImageDocs.add(new Document(imageDescription, imgMetadata));
            }

            Metadata pageMetadata = new Metadata();
            pageMetadata.put("page", pageNumber);
            pageMetadata.put("media", String.join(",", imagePathsOnPage));
            pageMetadata.put("type", "page_text");

            Document pageDoc = new Document(textContent, pageMetadata);
            embeddingStoreIngestor.ingest(pageDoc);

            allImageDescriptionDocs.addAll(pageImageDocs);
        }

        document.close();

        if (!allImageDescriptionDocs.isEmpty()) {
            System.out.println("[agentConfig] Ingesting " + allImageDescriptionDocs.size() + " image description(s).");
            embeddingStoreIngestor.ingest(allImageDescriptionDocs);
        }
    }
//            1. **Initialisation** :
//            - Création de la mémoire du chat.
//            - Chargement du modèle d’embedding et connexion à PostgreSQL.
//            2. **Vérification du vector store** :
//            - Si déjà peuplé, on ne recharge pas le PDF.
//            3. **Lecture du PDF** :
//            - Extraction page par page.
//            - Extraction du texte.
//            - Extraction des images et conversion en base64.
//            4. **Interaction avec le modèle de langage** :
//            - Génération de descriptions textuelles pour chaque image.
//            5. **Ingestion dans le vector store** :
//            - Texte + images + descriptions → documents vectorisés pour RAG.
//            6. **Résultat** :
//            - Tu peux ensuite utiliser ContentRetriever pour retrouver du texte ou des images pertinentes en fonction d’une requête.
}