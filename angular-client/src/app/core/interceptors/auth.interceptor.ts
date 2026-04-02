import { HttpInterceptorFn } from "@angular/common/http";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = localStorage.getItem('token');
    const lang = localStorage.getItem('lang') || 'en';

    const headers: Record<string, string> = { 'Accept-Language': lang };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const cloned = req.clone({ setHeaders: headers });
    return next(cloned);
}