import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, catchError, lastValueFrom, tap } from 'rxjs';
import { Character } from '../model';

@Component({
  selector: 'app-character-details',
  templateUrl: './character-details.component.html',
  styleUrls: ['./character-details.component.css']
})
export class CharacterDetailsComponent implements OnInit, OnDestroy{
 
  param$!: Subscription
  charId!: string  // can be string or number, both works
  character!: Character
  notFound = false

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private httpClient:HttpClient){}

  ngOnDestroy(): void {
    if(this.param$){
      this.param$.unsubscribe()
    }
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe({
      next: async (params) => {
        this.charId = params['charId']
        try{
          await this.getCharacterDetails()
          console.log('returned from getCharacterDetails(): ', this.character)
        }catch(err){
          this.notFound = true
          console.log('>> catch lastValueFrom error, reject value: ',err)
        }
        
      },
      error: err => {
        console.error(err)
      }
    })
  }

  // GET /api/character/<characterId>
  // Accept: application/json
  getCharacterDetails() {

    const headers = new HttpHeaders().set('Accept', 'application/json');

    // return as promise
    return lastValueFrom(
      this.httpClient.get('/api/character/' + this.charId, { headers })
                      .pipe( 
                        tap(v => this.character = v  as Character),
                        catchError(
                          async (err) => {
                            console.error('in catchError:', err); 
                            throw "Error parsing http response as Character"
                          }
                        )
                      )

    )
  }

  addComment(){

    this.router.navigate(['/comment'],{ queryParams: { charId: this.charId}})
  }

  

}
