package hello.login.web.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Slf4j
@RestController
public class SessionInfoController {

    @GetMapping("/session-info")
    public String sessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "세션이 없습니다.";
        }

        // 세션 데이터 출력
        session.getAttributeNames().asIterator()
                .forEachRemaining(name -> log.info("session name={}, value={} ", name, session.getAttribute(name)));

        log.info("sessionId={}", session.getId()); // 세션 아이디
        log.info("getMaxInactiveInterval={}", session.getMaxInactiveInterval()); //
        log.info("creationTime={}", new Date(session.getCreationTime())); // 세션 생성 시간
        log.info("lastAccessedTime={}", new Date(session.getLastAccessedTime()));// 마지막에 세션에 접근한 시간
        log.info("inNew={}", session.isNew()); // 새로운 세션인지

        return "세션 출력";

    }
}
