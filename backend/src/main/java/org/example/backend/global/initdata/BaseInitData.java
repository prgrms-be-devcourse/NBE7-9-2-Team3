package org.example.backend.global.initdata;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.repository.FollowRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.repository.PostRepository;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Profile("init-data") // init-data 프로파일이 활성화될 때만 실행
public class BaseInitData {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final AquariumRepository aquariumRepository;
    private final FishRepository fishRepository;
    private final PostRepository postRepository;
    private final TradeRepository tradeRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    CommandLineRunner initBaseData() {
        return args -> {
            // 테스트 유저 데이터 생성
            createTestUsers();

            // 팔로우 관계 생성
            createFollowRelationships();

            // 어항 및 물고기 데이터 생성
            createAquariumsAndFish();

            // 게시글 데이터 생성
            createPosts();

            // 거래 게시글 데이터 생성
            createTrades();
        };
    }

    private void createTestUsers() {
        // 이미 데이터가 있는지 확인
        if (memberRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 10; i++) {
            String email = "test" + i + "@test.com";
            String nickname = "test" + i;
            String password = "test1234";

            // 이미 존재하는지 확인
            if (memberRepository.findByEmail(email).isPresent()) {
                continue;
            }

            Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .profileImage(null)
                .build();

            memberRepository.save(member);
        }
    }

    private void createFollowRelationships() {
        // 이미 팔로우 관계가 있는지 확인
        if (followRepository.count() > 0) {
            return;
        }

        // test1부터 test10까지의 유저들을 조회
        for (int i = 1; i <= 10; i++) {
            Member follower = memberRepository.findByMemberId((long) i).orElse(null);

            if (follower == null) {
                continue;
            }

            // 각 유저가 다음 번호 유저 2명을 팔로우
            for (int j = 1; j <= 2; j++) {
                int followeeNumber = (i + j - 1) % 10 + 1; // 순환 로직
                Member followee = memberRepository.findByMemberId((long) followeeNumber).orElse(null);

                if (followee != null && !follower.getMemberId().equals(followee.getMemberId())) {
                    // 이미 팔로우 관계가 있는지 확인
                    if (!followRepository.existsByFollowerMemberIdAndFolloweeMemberId(
                        follower.getMemberId(), followee.getMemberId())) {

                        Follow follow = Follow.builder()
                            .follower(follower)
                            .followee(followee)
                            .build();

                        followRepository.save(follow);
                    }
                }
            }
        }
    }

    private void createAquariumsAndFish() {
        // 이미 어항 데이터가 있는지 확인
        if (aquariumRepository.count() > 0) {
            return;
        }

        String[] fishSpecies = {"금붕어", "구피", "네온테트라", "베타", "앵거피시", "플래티", "몰리", "다니오", "라스보라", "카디널테트라"};
        String[] fishNames = {"아름이", "예쁜이", "귀여니", "멋쟁이", "똑똑이", "영리이", "발랄이", "활발이", "사랑이", "행복이"};

        // test1부터 test10까지의 유저들에 대해 어항과 물고기 생성
        for (int i = 1; i <= 10; i++) {
            Member member = memberRepository.findByMemberId((long) i).orElse(null);

            if (member == null) {
                continue;
            }

            // 각 유저당 2개의 어항 생성
            for (int aquariumNum = 1; aquariumNum <= 2; aquariumNum++) {
                String aquariumName = "test" + i + "의 어항 " + aquariumNum;
                Aquarium aquarium = new Aquarium(member, aquariumName);
                Aquarium savedAquarium = aquariumRepository.save(aquarium);

                // 각 어항에 3마리의 물고기 생성
                for (int fishNum = 1; fishNum <= 3; fishNum++) {
                    String species = fishSpecies[(i + fishNum - 1) % fishSpecies.length];
                    String name = fishNames[(i + fishNum - 1) % fishNames.length] + fishNum;

                    Fish fish = new Fish(savedAquarium, species, name);
                    fishRepository.save(fish);
                }
            }
        }
    }

    private void createPosts() {
        // 이미 게시글 데이터가 있는지 확인
        if (postRepository.count() > 0) {
            return;
        }

        String[] postTitles = {
            "물고기 키우기 초보자를 위한 팁",
            "어항 관리의 중요성",
            "수질 관리 방법",
            "물고기 건강 체크리스트",
            "어항 장식 아이디어"
        };

        String[] postContents = {
            "물고기를 처음 키우시는 분들을 위한 기본적인 관리 방법을 알려드립니다.",
            "깨끗한 어항을 유지하는 것이 물고기 건강의 핵심입니다.",
            "적절한 수질을 유지하기 위한 다양한 방법들을 소개합니다.",
            "물고기의 건강 상태를 확인하는 방법들을 정리했습니다.",
            "어항을 더 아름답게 꾸미는 다양한 아이디어를 공유합니다."
        };

        BoardType[] boardTypes = {BoardType.SHOWOFF, BoardType.QUESTION};

        // test1부터 test10까지의 유저들에 대해 게시글 생성
        for (int i = 1; i <= 10; i++) {
            Member member = memberRepository.findByMemberId((long) i).orElse(null);

            if (member == null) {
                continue;
            }

            // 각 보드타입별로 5개씩 게시글 생성
            for (BoardType boardType : boardTypes) {
                for (int postNum = 1; postNum <= 5; postNum++) {
                    String title = boardType.name() + " - " + postTitles[(i + postNum - 1) % postTitles.length] + " " + postNum;
                    String content = postContents[(i + postNum - 1) % postContents.length] +
                        " (작성자: test" + i + ", 보드타입: " + boardType.name() + ")";

                    // PostWriteRequestDto를 사용하여 Post 생성
                    PostWriteRequestDto requestDto = new PostWriteRequestDto(
                        title,
                        content,
                        boardType.name(),
                        null, // 이미지는 null로 설정
                        null
                    );

                    Post post = new Post(requestDto, member);

                    postRepository.save(post);
                }
            }
        }
    }

    private void createTrades() {
        // 이미 거래 게시글 데이터가 있는지 확인
        if (tradeRepository.count() > 0) {
            return;
        }

        String[] tradeTitles = {
            "금붕어 판매합니다",
            "어항 세트 급처",
            "물고기 사료 판매",
            "어항 장식품 판매",
            "수질 테스트 키트 판매"
        };

        String[] tradeContents = {
            "건강한 금붕어를 판매합니다. 초보자도 키우기 쉽습니다.",
            "사용하던 어항 세트를 급하게 판매합니다. 상태 양호합니다.",
            "고품질 물고기 사료를 저렴하게 판매합니다.",
            "어항을 예쁘게 꾸밀 수 있는 장식품들을 판매합니다.",
            "수질을 정확히 측정할 수 있는 테스트 키트를 판매합니다."
        };

        org.example.backend.domain.trade.enums.BoardType[] tradeBoardTypes = {
            org.example.backend.domain.trade.enums.BoardType.FISH,
            org.example.backend.domain.trade.enums.BoardType.SECONDHAND
        };

        // test1부터 test10까지의 유저들에 대해 거래 게시글 생성
        for (int i = 1; i <= 10; i++) {
            Member member = memberRepository.findByMemberId((long) i).orElse(null);

            if (member == null) {
                continue;
            }

            // 각 보드타입별로 5개씩 거래 게시글 생성
            for (org.example.backend.domain.trade.enums.BoardType boardType : tradeBoardTypes) {
                for (int tradeNum = 1; tradeNum <= 5; tradeNum++) {
                    String title = boardType.getDescription() + " - " + tradeTitles[(i + tradeNum - 1) % tradeTitles.length] + " " + tradeNum;
                    String content = tradeContents[(i + tradeNum - 1) % tradeContents.length] +
                        " (판매자: test" + i + ", 보드타입: " + boardType.getDescription() + ")";

                    Trade trade = new Trade(
                        member,
                        boardType,
                        title,
                        content,
                        10000L + (i * 1000L) + (tradeNum * 100L), // 가격 (10000원부터 시작)
                        org.example.backend.domain.trade.enums.TradeStatus.SELLING,
                        boardType.getDescription(),
                        java.time.LocalDateTime.now()
                    );

                    tradeRepository.save(trade);
                }
            }
        }
    }
}
