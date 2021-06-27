package com.myproject.myweb.service;

import com.myproject.myweb.domain.Like;
import com.myproject.myweb.domain.Post;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.like.LikeRequestDto;
import com.myproject.myweb.dto.like.LikeResponseDto;
import com.myproject.myweb.repository.like.LikeRepository;
import com.myproject.myweb.repository.post.PostRepository;
import com.myproject.myweb.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Slf4j
public class LikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    // @Transactional(readOnly = true)
    public LikeResponseDto findLikeOne(Long postId, Long userId){
        Like entity = likeRepository.findLikeOne(postId, userId)
                .orElseThrow(() -> new IllegalStateException("LikeNotFoundException"));

        return new LikeResponseDto(entity);
    }

    @Transactional
    public Long save(LikeRequestDto likeRequestDto){
        Post post = postRepository.findById(likeRequestDto.getPostId())
                .orElseThrow(() -> new IllegalStateException());
        User user = userRepository.findById(likeRequestDto.getUserId())
                .orElseThrow(() -> new IllegalStateException());

        Long id = likeRepository.save(
                Like.builder()
                .post(post)
                .user(user)
                .build()
        ).getId();

        return id;
    }


    @Transactional
    public void push(LikeRequestDto likeRequestDto){

        Long zero = Long.valueOf(0);
        Long id = likeRepository.findLikeOne(likeRequestDto.getPostId(), likeRequestDto.getUserId())
                .map(l -> l.getId())
                .orElseGet(() -> zero);

        log.info(String.valueOf(id));

        if(id != zero){
            this.delete(id);

        }else{
            this.save(likeRequestDto);
        }
    }

    @Transactional
    public void delete(Long id){
        Like like = likeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException());

        like.getPost().likeDelete(like);
        like.getUser().likeDelete(like);

        likeRepository.delete(like);

    }

    // 특정 게시글 좋아요 개수 - controller detail 뷰에 넣기
    public Long findLikeInPost(Long postId){
        return likeRepository.countAllByPost_Id(postId)
                .orElseThrow(() -> new IllegalStateException());
    }

    // user가 자신의 게시글로 받은 모든 like 개수
    public Long findLikeInUser(Long userId){
        return likeRepository.countAllByPostWriter(userId)
                .orElseThrow(() -> new IllegalStateException());
    }
}