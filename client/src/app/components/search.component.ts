import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit{

  form!:FormGroup

  constructor(private fb:FormBuilder, private router:Router){}

  ngOnInit(): void {
    this.form = this.createForm()
  }

  createForm():FormGroup{
    return this.fb.group({
      characterName: this.fb.control<string>('',[Validators.required])
    })
  }

  search(){
    this.router.navigate(['/characterList',this.form.value['characterName']])
  }
}
