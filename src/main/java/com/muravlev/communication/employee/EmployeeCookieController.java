package com.muravlev.communication.employee;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Пример: логин через cookie, без localStorage.
 */
@RestController
@RequestMapping("/api/employees/cookie")
public class EmployeeCookieController {

    private final EmployeeService employeeService;

    public EmployeeCookieController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Логин через cookie (POST /api/employees/cookie/login).
     * Если логин успешен, ставим Cookie "username=..."
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginCookie(
            @RequestBody Map<String, String> creds,
            HttpServletResponse response
    ) {
        String username = creds.get("username");
        String password = creds.get("password");

        if (employeeService.authenticate(username, password)) {
            // Успех - устанавливаем куку
            Cookie cookie = new Cookie("username", username);
            cookie.setPath("/");           // Кука видна всему сайту
            cookie.setHttpOnly(false);     // Можно выставить true, если нужно
            // cookie.setMaxAge(...)       // Можно задать время жизни
            // cookie.setSecure(true);     // Если используете HTTPS

            response.addCookie(cookie);

            return ResponseEntity.ok("Cookie set, login successful.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user or password.");
        }
    }

    /**
     * "Кто я?" (GET /api/employees/cookie/whoami).
     * Смотрим Cookie "username". Если есть, возвращаем имя, иначе 401.
     */
    @GetMapping("/whoami")
    public ResponseEntity<String> whoAmI(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("username".equals(c.getName())) {
                    // Нашли куку, возвращаем
                    String user = c.getValue();
                    return ResponseEntity.ok(user);
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No cookie found");
    }

    /**
     * Логаут - просто стираем cookie (ставим maxAge=0).
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("username", "");
        cookie.setPath("/");
        cookie.setMaxAge(0); // стираем
        response.addCookie(cookie);
        return ResponseEntity.ok("Cookie cleared, logged out");
    }
}

