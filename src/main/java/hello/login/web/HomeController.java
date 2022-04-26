package hello.login.web;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

//    @GetMapping("/")
    public String home() {
//        return "redirect:/items";
        return "home";
    }

    // 쿠키를 받아 로그인 처리가 되는 홈화면
    //@GetMapping("/")
    public String HomeLogin(@CookieValue(name= "memberId", required = false) Long memberId , Model model){
        // 로그인 안한 사용자도 들어오기 떄문에 required = false 를 해줌
        //memberId : String -> Long 으로 TypeConverting. @RequestParam 참고
        if (memberId == null) {
            return "home";
        }

        //로그인 성공한 사용자 (쿠키 있음)
        Member loginMember = memberRepository.findById(memberId);

        // 없으면 home
        if (loginMember == null) {
            return "home";
        }

        model.addAttribute("member", loginMember);
        return "loginHome"; // 로그인 사용자 전용 화면 홈
    }

    // 요청정보의 쿠키의 세션 id를 통해 조회
    // @GetMapping("/")
    public String HomeLoginV2(HttpServletRequest request, Model model){

        // 세션 관리자에 저장된 회원 정보 조회
        Member member = (Member) sessionManager.getSession(request);

        // 세션에 회원 데이터가 없으면 home
        if (member == null) {
            return "home";
        }

        //로그인 성공한 사용자
        model.addAttribute("member", member);
        return "loginHome"; // 로그인 사용자 전용 화면 홈
    }

    // 요청정보의 쿠키의 세션 id를 통해 조회
    //@GetMapping("/")
    public String HomeLoginV3(HttpServletRequest request, Model model){

        HttpSession session = request.getSession(false); // true 는 세션이 없으면 생성할 의도가 없어도 생성해버리기 때문에
        if (session == null){
            return "home";
        }

        Member loginMember = (Member)session.getAttribute(SessionConst.LOGIN_MEMBER);

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 loginHome
        model.addAttribute("member", loginMember);
        return "loginHome"; // 로그인 사용자 전용 화면 홈
    }

    // 요청정보의 쿠키의 세션 id를 통해 조회
    @GetMapping("/")
    public String HomeLoginV3Spring(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, Model model){

        // 기존의 코드 로직을 애노테이션 하나로 해결하게 됨
//        HttpSession session = request.getSession(false); // true 는 세션이 없으면 생성할 의도가 없어도 생성해버리기 때문에
//        if (session == null){
//            return "home";
//        }
//
//        Member loginMember = (Member)session.getAttribute(SessionConst.LOGIN_MEMBER);

        //세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        //세션이 유지되면 loginHome
        model.addAttribute("member", loginMember);
        return "loginHome"; // 로그인 사용자 전용 화면 홈
    }
}