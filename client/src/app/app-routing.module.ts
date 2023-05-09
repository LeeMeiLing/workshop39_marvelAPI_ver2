import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchComponent } from './components/search.component';
import { CharacterDetailsComponent } from './components/character-details.component';
import { CharacterListComponent } from './components/character-list.component';
import { CommentComponent } from './components/comment.component';

const routes: Routes = [
  { path:'', component:SearchComponent},
  { path:'characterList/:charName', component:CharacterListComponent},
  { path:'characterDetails/:charId', component:CharacterDetailsComponent},
  { path:'comment', component:CommentComponent},
  { path:'**', redirectTo:'/', pathMatch:'full'}
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
