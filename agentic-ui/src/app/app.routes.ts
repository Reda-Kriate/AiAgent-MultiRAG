import { Routes } from '@angular/router';
import {TransactionsComponent} from "./transactions/transactions.component";
import {AgentUiComponent} from "./agent-ui/agent-ui.component";

export const routes: Routes = [
  {
    path : "transactions", component : TransactionsComponent,
  },
  {
    path : "agent", component : AgentUiComponent,
  },
];
