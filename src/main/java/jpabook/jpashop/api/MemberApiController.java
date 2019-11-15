package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    //Entity를 Parameter로 직접 사용 (X)
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return  new CreateMemberResponse(id);
    }

    //별도의 클래스, DTO를 이용 (O)
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse UpdateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){
        memberService.update(id,request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    //조회 테스트 (아이디 검색)
    @PostMapping("/api/v2/members/{id}")
    public SearchMemberResponse searchMemberResponse(@PathVariable("id") Long id){
        Member findMember = memberService.findOne(id);
        return new SearchMemberResponse(id,findMember.getName());
    }

    //조회 테스트 (전체)

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result<>(collect);
    }

    //조회 테스트 (전체)
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
    //조회 전체 DTO
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    //조회 테스트 아이디 검색 Response
    @Data
    @AllArgsConstructor
    static class SearchMemberResponse {
        private Long id;
        private String name;
    }


    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id){
            this.id = id;
        }
    }
}
