import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { RegisterService } from '../../services/auth/register/register.service';
import { RegisterRequest } from '../../services/auth/register/registerRequest';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule, CommonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {

  registerError:string="";

  registerForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    password: ['', [Validators.required, Validators.maxLength(128), Validators.minLength(8)]],
    repeatPassword: ['', [Validators.required, Validators.maxLength(128), Validators.minLength(8)]],
    name: ['', [Validators.required, Validators.maxLength(64)]],
    lastName: ['', [Validators.required, Validators.maxLength(64)]],
    community: ['', [Validators.required, Validators.maxLength(64)]],
    province: ['', [Validators.required, Validators.maxLength(64)]],
    phone: ['', [Validators.maxLength(15)]],
    job: ['', [Validators.maxLength(64)]],
    experience: ['', [Validators.maxLength(2)]],
  });
  constructor(private formBuilder: FormBuilder, private router:Router, private registerService:RegisterService) {  }

  register(){
    if(this.registerForm.valid){
      this.registerService.register(this.registerForm.value as RegisterRequest).subscribe({
        next: (userData) => {console.log(userData);},
        error: (errorData) => {
          console.error(errorData);
          this.registerError = errorData;
        },
        complete: () => {
          console.info("Registro completado");
          this.router.navigateByUrl('/login');
          this.registerForm.reset();
        }
      });
      
    }
    else{
      alert("Error al ingresar los datos");
    }
  }

  get email(){
    return this.registerForm.controls.email;
  }

  get password(){
    return this.registerForm.controls.password;
  }

  get repeatPassword(){
    return this.registerForm.controls.repeatPassword;
  }

  get name(){
    return this.registerForm.controls.name;
  }

  get lastName(){
    return this.registerForm.controls.lastName;
  }

  get community(){
    return this.registerForm.controls.community;
  }

  get province(){
    return this.registerForm.controls.province;
  }

  get phone(){
    return this.registerForm.controls.phone;
  }

  get job(){
    return this.registerForm.controls.job;
  }

  get experience(){
    return this.registerForm.controls.experience;
  }
}
