import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { JwtInterceptorService } from './services/auth/jwt-interceptor/jwt-interceptor.service';
import { ErrorInterceptorService } from './services/auth/error-interceptor/error-interceptor.service';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    {provide:HTTP_INTERCEPTORS, useClass:JwtInterceptorService, multi:true},
    {provide:HTTP_INTERCEPTORS, useClass:ErrorInterceptorService, multi:true}, provideAnimationsAsync(),
  ]
};
