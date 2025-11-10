package br.appAdoteJa.appAdoteJa.service.autenticator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import br.appAdoteJa.appAdoteJa.service.CookieService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
	
    @Autowired
    private CookieService cookieService; 
    
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException{
		
		try {

            String usuarioId = cookieService.getCookie(request, "usuarioId");

			if(usuarioId != null && !usuarioId.isEmpty()) {
				return true;
			}
		} catch (UnsupportedEncodingException e) {
            e.printStackTrace(); 
		}

		response.sendRedirect("/login");
		return false;
		
	}
}