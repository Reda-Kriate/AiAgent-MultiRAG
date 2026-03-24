import {Component, input} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {HttpClient, HttpDownloadProgressEvent, HttpEventType} from "@angular/common/http";
import {MarkdownComponent} from "ngx-markdown";
import {of} from "rxjs";

@Component({
  selector: 'app-agent-ui',
  standalone: true,
  imports: [
    FormsModule,
    MarkdownComponent
  ],
  templateUrl: './agent-ui.component.html',
  styleUrl: './agent-ui.component.css'
})
export class AgentUiComponent {
  response: any;
  question: any;
  protected images: any;

  constructor(private http : HttpClient) {
  }

  askAgent() {
    this.http.get("http://localhost:8091/ask?question="+this.question,
      {responseType:"text",observe:"events", reportProgress:true }).subscribe({
      next : resp => {
        if (resp.type === HttpEventType.DownloadProgress) {
          this.response = (resp as HttpDownloadProgressEvent).partialText
        }
      console.log(this.response)
      },
      error : err => {console.log(err)},
      complete:()=>{
        this.extractImgPath(this.response)
      }
    })
  }

  private extractImgPath(input: string) {
    const regex = /SOURCE_IMAGE\(([^)]+)\)=>/g;
    let match ;
    while ((match=regex.exec(input)) !== null ){
      this.images.push("http://localhost:8091/image?path="+match[1])
    }
  }

  protected readonly of = of;
}
