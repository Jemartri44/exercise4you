import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-verification',
  standalone: true,
  imports: [ ReactiveFormsModule, CommonModule ],
  templateUrl: './verification.component.html',
  styleUrl: './verification.component.css'
})
export class VerificationComponent {

  verificationError:string="";
  verificationState: string='';
  verificationInfo: string='';
  expired: boolean = false;

  constructor(private formBuilder: FormBuilder, private router:Router, private authService:AuthService ) { 
    let token = this.router.url.split('=')[1]
    console.debug(token)
    if(token == undefined || token == null || token == '') {
      return
    }
    this.authService.checkVerificationToken(this.router.url.split('=')[1]).subscribe({
      next: (userData) => {console.log(userData);},
      error: (errorData) => {
        console.error(errorData);
        if(errorData.message == "El usuario ya está verificado"){
          this.router.navigateByUrl('/login?verified=true');
        }
        this.verificationError = errorData.message;
      },
      complete: () => {
        this.router.navigateByUrl('/login?verified=true');
      }

    });
  }
  
  verificationForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]]
  });

  resendVerificationEmail(){
    if(!this.verificationForm.valid){
      this.verificationError = "Introduzca un correo electrónico válido";
      return;
    }
    this.verificationState = 'LOADING';
    this.authService.resendVerificationEmail(this.verificationForm.value as string).subscribe({
      next: (userData) => {console.log(userData);},
      error: (errorData) => {
        this.verificationState = 'ERROR';
        console.error(errorData);
        this.verificationError = errorData.message;
      },
      complete: () => {
        console.info("Correo de verificación reenviado");
        this.verificationState = '';
        this.router.navigateByUrl('/login?registered=true');
      }
    });
  }
  
  get email(){
    return this.verificationForm.controls.email;
  }
}

