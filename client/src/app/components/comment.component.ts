import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Comment } from '../model';

@Component({
  selector: 'app-comment',
  templateUrl: './comment.component.html',
  styleUrls: ['./comment.component.css']
})
export class CommentComponent implements OnInit, OnDestroy {

  queryParams$!: Subscription
  charId!: string
  form!:FormGroup
  returned!: Comment

  constructor(private router:Router, private activatedRoute: ActivatedRoute, private httpClient: HttpClient, private fb:FormBuilder){}

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe({
      next: async (queryParams) => { 
        this.charId = queryParams['charId']
        console.log('charId in comment OnInit ', this.charId)
        this.form = this.createForm()
      }
      ,
      error: (err)  => console.error(err)
    })
  }

  ngOnDestroy(): void {
    if(this.queryParams$){
      this.queryParams$.unsubscribe()
    }
  }

  createForm():FormGroup{
    return this.fb.group({
      comment:this.fb.control<string>('',[Validators.required])
    })
  }

  // POST /api/character/<characterId>
  // Content-Type: application/json
  // Accept: application/json
  addComment(){

    const headers = new HttpHeaders().set('Content-Type','application/json').set('Accept','application/json');
    const payload = { comment: this.form.value['comment']}
    console.log('comment: ', this.form.value['comment']); // debug

    // DONT use payload.toString() !!!
    this.httpClient.post<Comment>(`/api/character/${this.charId}`, payload, {headers})  
                      .subscribe({ 
                        next: (v:any) => {
                          this.returned = v as Comment;
                          console.log('Comment returned from server: ', this.returned)
                          // this.router.navigate(['/characterDetails' , this.charId])
                        },
                        error: (err) => console.error('in subscribe error:', err),
                        complete: () => {
                          console.log('httpClient subscribe completed')
                          this.router.navigate(['/characterDetails' , this.charId])
                        }
                      })   

  }

}
