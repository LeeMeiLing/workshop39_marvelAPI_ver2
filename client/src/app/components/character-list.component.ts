import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, catchError, lastValueFrom, map, tap} from 'rxjs';
import { Character } from '../model';

@Component({
  selector: 'app-character-list',
  templateUrl: './character-list.component.html',
  styleUrls: ['./character-list.component.css']
})
export class CharacterListComponent implements OnInit, OnDestroy{

  param$!: Subscription
  charName!: string
  characterList: Character[] = []
  limit = 20
  offset = 0
  notFound = false
  currentPage = 0
  disablePrevious = true
  disableNext = false

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private httpClient:HttpClient){}

  ngOnDestroy(): void {
    if(this.param$)
      this.param$.unsubscribe()
  }

  ngOnInit(): void {
    this.param$ = this.activatedRoute.params.subscribe({
      next: async (params) => {
          this.charName = params['charName'];
          try{
            await this.getCharacterList();
            if(this.characterList.length < 20){
              this.disableNext = true
            }
            console.log('>> characterList returned from http request: ', this.characterList)
          }catch(err){
            this.notFound = true
            console.log('>> catch lastValueFrom error, reject value: ',err)
          }
      }
      ,
      error: err => {
        console.error(err)
      }
    })
  }

  // GET /api/characters
  // Accept: application/json
  getCharacterList():Promise<any>{
    
    const headers = new HttpHeaders().set('Accept','application/json');
    const params = new HttpParams().set('charName',this.charName).set('limit',this.limit).set('offset',this.offset);
    return lastValueFrom(
      this.httpClient.get<Character[]>('/api/characters', { headers, params }).pipe(
        tap((r: any) => this.characterList = r as Character[]),
        catchError(
          async (err) => {
            console.error('in catchError:', err); 
            throw "Error parsing http response as Character[ ]"
          }
        )
      )
    )

  }

  async previous(){

    this.characterList = []
    this.disableNext = false;
    this.currentPage--;

    if(this.currentPage == 0){

      this.disablePrevious = true;

    }

    this.offset = this.offset - 20;

    try{
      await this.getCharacterList();
      if(this.characterList.length < 20){
        this.disableNext = true
      }
      console.log('>> characterList returned from http request: ', this.characterList)
    }catch{
      this.notFound = true
      console.log('>> in catch')
    }
    
  }

  async next(){
   
    this.characterList = []
    this.disablePrevious = false;
    this.currentPage++;
    this.offset = this.offset + 20;

    try{
      await this.getCharacterList();
      if(this.characterList.length < 20){
        this.disableNext = true
      }
      console.log('>> characterList returned from http request: ', this.characterList)
    }catch{
      this.notFound = true
      this.disableNext = true
      console.log('>> in catch')
    }

  }

}
