package hello.login.web.session;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 관리
 */
@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "mySessionId"; // comm + option + c
    private Map<String, Object> sessionStore = new ConcurrentHashMap<>();

    /**
     * 1. 세션 생성
     * sessionId 생성 (임의의 추정 불가능한 랜덤 값)
     * 세션 저장소에 sessionId와 보관할 값 저장
     * sessionId로 응답 쿠키를 생성해서 클라이언트에 전달
     */
    public void createSession(Object value, HttpServletResponse response) {
        // 세션 id를 생성하고, 값을 세션에 저장
        String sessionId = UUID.randomUUID().toString();//UUID 는 임의의 값을 생성시켜줌(자바에서 제공)
        sessionStore.put(sessionId, value);

        //쿠키 생성
        Cookie mySessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        response.addCookie(mySessionCookie);

    }

    /**
     * 2. 세션 조회
     * 클라이언트가 요청한 sessionId 쿠키의 값으로, 세션 저장소에 보관한 값 조회
     */
    public Object getSession(HttpServletRequest request) {
        // 해당 로직을 findCookie로 분리햇음 : 쿠키가 배열로 반환되기 때문에 for문을 써서 찾는 과정들이 너무 번거로움
//        Cookie[] cookies = request.getCookies();
//        if (cookies == null) {
//            return null;
//        }
//        for(Cookie cookie : cookies){
//            if (cookie.getName().equals(SESSION_COOKIE_NAME)) {
//                return sessionStore.get(cookie.getValue());
//            }
//        }

        Cookie sessionCookie = findCookie(request, SESSION_COOKIE_NAME);
        if (sessionCookie == null){
            return null;
        }
        return sessionStore.get(sessionCookie.getValue()); // sessionCookie.getValue() : 세션 id
    }

    /**
     * 3. 세션 만료
     * 클라이언트가 요청한 session 쿠키의 값으로, 세션 저장소에 보관한 sessionId와 값 제거
     */
    public void expire(HttpServletRequest request) {
        Cookie sessionCookie = findCookie(request, SESSION_COOKIE_NAME);
        if (sessionCookie != null) {
            sessionStore.remove(sessionCookie.getValue()); // 세션저장소에서 삭제
        }
    }


    public Cookie findCookie(HttpServletRequest request, String cookieName) {
        //Cookie[] cookies = request.getCookies();

        if (request.getCookies() == null) {
            return null;
        }
        //Arrays.Stream : 배열을 스트림으로 변경해줌
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findAny()
                .orElse(null);
    }

}
