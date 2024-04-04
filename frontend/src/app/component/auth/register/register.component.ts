import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { RegisterRequest } from '../../../services/auth/registerRequest';
import { AuthService } from '../../../services/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule, CommonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {

  registerError:string="";
  registerState: string='';
  expired: boolean = false;

  constructor(private formBuilder: FormBuilder, private router:Router, private authService:AuthService, private route: ActivatedRoute ) {  }

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

  register(){
    if(!this.registerForm.valid || this.password.value != this.repeatPassword.value){
      this.registerError = "Por favor, rellene todos los campos obligatorios correctamente";
      return;
    }
    this.registerState = 'LOADING';
    this.authService.register(this.registerForm.value as RegisterRequest).subscribe({
      next: (userData) => {console.log(userData);},
      error: (errorData) => {
        this.registerState = 'ERROR';
        console.error(errorData);
        this.registerError = errorData.message;
      },
      complete: () => {
        this.registerState = '';
        console.info("Registro completado");
        this.router.navigateByUrl('/login');
        this.registerForm.reset();
      }
    });
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
