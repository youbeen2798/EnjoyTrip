package notion.service;

import common.exception.NotionException;
import common.validation.NotionValidation;
import common.validation.dto.NotionRequest;
import member.Authority;
import member.Member;
import member.repository.MemberJdbcRepository;
import member.repository.MemberRepository;
import notion.Notion;
import notion.dto.NotionDto;
import notion.repository.NotionJdbcRepository;
import notion.repository.NotionRepository;

import java.util.Optional;

public class NotionServiceImpl implements NotionService {

    private static final NotionService notionService = new NotionServiceImpl();
    private final NotionRepository notionRepository;
    private final MemberRepository memberRepository;

    private NotionServiceImpl() {
        notionRepository = NotionJdbcRepository.getNotionRepository();
        memberRepository = MemberJdbcRepository.getMemberRepository();
    }

    public static NotionService getNotionService() {
        return notionService;
    }


    @Override
    public int addNotion(Long memberId, NotionDto notionDto) {
        NotionValidation validation = new NotionValidation();

        NotionRequest request = NotionRequest.builder()
                .title(notionDto.getTitle())
                .content(notionDto.getContent())
                .build();
        validation.validate(request);

        Optional<Member> findMember = memberRepository.findById(memberId);
        if (!findMember.isPresent()) {
            throw new NotionException();
        }

        Member member = findMember.get();
        if (member.getAuthority() != Authority.ADMIN) {
            throw new NotionException();
        }

        return notionRepository.save(memberId, notionDto);
    }

    @Override
    public int editNotion(Long notionId, Long memberId, NotionDto notionDto) {
        NotionValidation validation = new NotionValidation();

        NotionRequest request = NotionRequest.builder()
                .title(notionDto.getTitle())
                .content(notionDto.getContent())
                .build();
        validation.validate(request);

        Optional<Member> findMember = memberRepository.findById(memberId);
        if (!findMember.isPresent()) {
            throw new NotionException();
        }

        Member member = findMember.get();
        if (member.getAuthority() != Authority.ADMIN) {
            throw new NotionException();
        }

        Optional<Notion> findNotion = notionRepository.findById(notionId);
        if (!findNotion.isPresent()) {
            throw new NotionException();
        }

        Notion notion = findNotion.get();
        notion.edit(notionDto.getTitle(), notionDto.getContent(), member);

        return notionRepository.update(notionId, notion);
    }

    @Override
    public int removeNotion(Long notionId, Long memberId) {
        Optional<Member> findMember = memberRepository.findById(memberId);
        if (!findMember.isPresent()) {
            throw new NotionException();
        }

        Member member = findMember.get();
        if (member.getAuthority() != Authority.ADMIN) {
            throw new NotionException();
        }

        Optional<Notion> findNotion = notionRepository.findById(notionId);
        if (!findNotion.isPresent()) {
            throw new NotionException();
        }

        return notionRepository.remove(notionId);
    }
}
