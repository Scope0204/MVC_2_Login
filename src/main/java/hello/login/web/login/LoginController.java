package hello.login.web.login;

import hello.login.domain.login.LoginService;
import hello.login.domain.member.Member;
import hello.login.web.SessionConst;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final SessionManager sessionManager;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
        return "login/loginForm";
    }

    //@PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
        log.info("login? {}", loginMember);

        if (loginMember == null) { // 검증조건은 맞으나, id와 패스워드 매칭이 다른경우
            bindingResult.reject("loginFail","아이디 또는 비밀번호가 맞지 않습니다"); // reject : 글로벌 오류
            return "login/loginForm";
        }

        // 로그인 성공 처리
        // 쿠키 생성 -> 시간 정보를 주지 않으면 -> 세션 쿠키(브라우저 종료시 모두 종료됨)
        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
        response.addCookie(idCookie);  //HTTP 응답에 쿠키를 같이 보내줌

        return "redirect:/";
    }

    //@PostMapping("/login")
    public String loginV2(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
        log.info("login? {}", loginMember);

        if (loginMember == null) { // 검증조건은 맞으나, id와 패스워드 매칭이 다른경우
            bindingResult.reject("loginFail","아이디 또는 비밀번호가 맞지 않습니다"); // reject : 글로벌 오류
            return "login/loginForm";
        }

        // 로그인 성공 처리 수정 -> 직접만든 세션저장소 사용
        //세션 관리자를 통해 세션을 생성하고, 회원 데이터 보관
        sessionManager.createSession(loginMember, response);

        return "redirect:/";
    }

    // 서블릿 세션 사용
    //@PostMapping("/login")
    public String loginV3(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
        log.info("login? {}", loginMember);

        if (loginMember == null) { // 검증조건은 맞으나, id와 패스워드 매칭이 다른경우
            bindingResult.reject("loginFail","아이디 또는 비밀번호가 맞지 않습니다"); // reject : 글로벌 오류
            return "login/loginForm";
        }

        // 로그인 성공 처리 수정 -> 직접만든 세션저장소가 아닌 서블릿이 지원하는 세션 저장소를 사용
        // getSession(true) : 세션이 있으면 있는 세션 반환, 없으면 신규 세션 생성 <-> false 는 새롭게 세션을 생성하지는 않음
        HttpSession session = request.getSession(); // default 로 true 값을 가짐
        // 세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
        return "redirect:/";
    }

    // 쿼리 파라미터를 받기 위해 @RequestParam 을 사용한다
    @PostMapping("/login")
    public String loginV4(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult,
                          @RequestParam(defaultValue = "/") String redirectURL,
                          HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
        log.info("login? {}", loginMember);

        if (loginMember == null) { // 검증조건은 맞으나, id와 패스워드 매칭이 다른경우
            bindingResult.reject("loginFail","아이디 또는 비밀번호가 맞지 않습니다"); // reject : 글로벌 오류
            return "login/loginForm";
        }

        // 로그인 성공 처리

        // 세션이 있으면 있는 세션 반환. 없으면 신규 세션 생성
        HttpSession session = request.getSession(); // default 로 true 값을 가짐
        // 세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        // 이제 로그인시 이전 접근했던 페이지로 이동하기 위해 redirectURL 적용
        return "redirect:" + redirectURL;
    }

    //@PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        log.info("로그아웃 되었습니다");
        expireCookie(response, "memberId"); // 만료된 쿠키 처리
        return "redirect:/";
    }

    //@PostMapping("/logout")
    public String logoutV2(HttpServletRequest request) {
        sessionManager.expire(request); // 세션 저장소에서 제거 -> 클라이언트에는 남아잇으나 이미 세션 저장소에 없기에 쓸모없음
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logoutV3(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // 있는 세션을 가져옴
        if (session != null){
            session.invalidate();
        }
        return "redirect:/";
    }

    // comm + option + m : 로직 분리
    // expire : 만료되다
    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
