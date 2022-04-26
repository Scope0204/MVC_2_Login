package hello.login.domain.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.*;

@Slf4j
@Repository
public class MemberRepository {

    /**
     * 원래라면 MemberRepository 를 인터페이스로 만드는 게 더 좋다
     * 구현체로 디비의 회원과 메모리 회원을 나눠 관리할 수 있으니
     */
    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;

    public Member save(Member member) {
        member.setId(++sequence);
        log.info("save: member={}", member);
        store.put(member.getId(), member);
        return member;
    }

    public Member findById(Long id) {
        return store.get(id);
    }

    public List<Member> findAll(){
        return new ArrayList<>(store.values()); // <key : value> 의 value 들만 가져옴
    }


    public Optional<Member> findByLoginId(String loginId){
/*    List<Member> all = findAll();
        for (Member m : all) {
            if (m.getLoginId().equals(loginId)) {
                return Optional.of(m);
            }
        }
        return Optional.empty(); */

        return findAll().stream()
                .filter(m -> m.getLoginId().equals(loginId))
                .findFirst(); // 먼저나오는 값을 반환
    }

    public void clearStore(){
        store.clear();

}

}