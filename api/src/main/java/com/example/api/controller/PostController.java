package com.example.api.controller;

import com.example.api.entity.Post;
import com.example.api.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Posts API のコントローラー
 *
 * 【学習ポイント】
 * @RestController = @Controller + @ResponseBody
 * - このクラスのメソッドは自動的に JSON を返す
 * - View（HTML）ではなく、データを返す API 向け
 *
 * 【重要：CORS について】
 * 現時点では CORS 設定をしていない。
 * そのため、ブラウザから localhost:3000 → localhost:8080 への
 * リクエストは CORS エラーになる。これは意図的。
 *
 * 【Reviewer観点】
 * - 認証なしで全操作可能 = 誰でもデータを変更できる（Phase 2 で対応）
 * - エラーハンドリングが簡素（本番では @ControllerAdvice で共通化）
 * - バリデーションなし（本番では @Valid を使う）
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;

    /**
     * コンストラクタインジェクション
     *
     * 【学習ポイント】
     * Spring の DI（依存性注入）により、PostRepository のインスタンスが
     * 自動的に渡される。new PostRepository() と書く必要がない。
     */
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 全件取得
     * GET /api/posts
     *
     * 【学習ポイント】
     * @GetMapping でこのメソッドが GET リクエストに対応することを示す。
     * 戻り値の List<Post> は自動的に JSON 配列に変換される。
     */
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 1件取得
     * GET /api/posts/{id}
     *
     * 【学習ポイント】
     * @PathVariable で URL の {id} 部分を引数として受け取る。
     * 存在しない場合は 404 を返す。
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新規作成
     * POST /api/posts
     *
     * 【学習ポイント】
     * @RequestBody で JSON リクエストボディを Post オブジェクトに変換。
     * Content-Type: application/json が必要（これが CORS の Preflight を発生させる原因の一つ）。
     */
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post saved = postRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 更新
     * PUT /api/posts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(post.getTitle());
                    existing.setContent(post.getContent());
                    return ResponseEntity.ok(postRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 削除
     * DELETE /api/posts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
