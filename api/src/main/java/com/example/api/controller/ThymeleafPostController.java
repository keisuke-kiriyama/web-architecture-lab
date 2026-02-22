package com.example.api.controller;

import com.example.api.entity.Post;
import com.example.api.repository.PostRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Thymeleaf を使った MPA 版投稿管理コントローラー
 *
 * 【学習ポイント】
 * @Controller（@RestController ではない）を使う。
 * - @Controller: View（HTML）を返す
 * - @RestController: JSON を返す
 *
 * 【SPA と MPA の比較】
 * | 項目 | SPA (Next.js) | MPA (Thymeleaf) |
 * |------|---------------|-----------------|
 * | HTML 生成 | クライアント | サーバー |
 * | ページ遷移 | JavaScript | フルリロード |
 * | データ取得 | fetch + JSON | Model に埋め込み |
 * | form 送信 | fetch + JSON | POST + リダイレクト |
 *
 * 【form POST の値渡し】
 * SPA: fetch でJSONを送る → レスポンスを受けて router.push
 * MPA: form POST → サーバーで処理 → リダイレクト（PRG パターン）
 *
 * 【PRG パターン（Post-Redirect-Get）】
 * 1. POST でフォーム送信
 * 2. サーバーで処理
 * 3. リダイレクト（302）で別 URL に誘導
 * 4. ブラウザが GET でリダイレクト先を取得
 *
 * なぜ PRG が必要か:
 * - POST 結果をそのまま返すと、ブラウザの「戻る」「更新」で再送信される
 * - リダイレクトすると、最後のリクエストが GET になり安全
 *
 * 【Reviewer 観点】
 * - CSRF 対策は必須（Thymeleaf + Spring Security で自動付与）
 * - バリデーションエラー時の処理
 * - 二重送信対策
 */
@Controller
@RequestMapping("/thymeleaf/posts")
public class ThymeleafPostController {

    private final PostRepository postRepository;

    public ThymeleafPostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 一覧ページ
     * GET /thymeleaf/posts
     *
     * 【学習ポイント】
     * Model にデータを入れると、テンプレートで参照できる。
     * return "posts/list" → templates/posts/list.html を探す。
     */
    @GetMapping
    public String list(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "posts/list";  // templates/posts/list.html
    }

    /**
     * 詳細ページ
     * GET /thymeleaf/posts/{id}
     *
     * 【学習ポイント】
     * パスパラメータでリソースを識別（REST 的）。
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        return "posts/detail";  // templates/posts/detail.html
    }

    /**
     * 新規作成フォーム表示
     * GET /thymeleaf/posts/new
     *
     * 【学習ポイント】
     * フォーム表示用に空の Post オブジェクトを渡す。
     * Thymeleaf の th:object でフォームとバインドする。
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/form";  // templates/posts/form.html
    }

    /**
     * 新規作成（POST）
     * POST /thymeleaf/posts
     *
     * 【学習ポイント: PRG パターン】
     * 1. POST でフォームデータを受け取る
     * 2. DB に保存
     * 3. リダイレクト（302）で一覧ページへ
     *
     * "redirect:" プレフィックスでリダイレクトレスポンスになる。
     * ブラウザは 302 を受け取ると、Location ヘッダーの URL に GET リクエストを送る。
     */
    @PostMapping
    public String create(@ModelAttribute Post post) {
        postRepository.save(post);
        return "redirect:/thymeleaf/posts";  // PRG: リダイレクトで一覧へ
    }

    /**
     * 編集フォーム表示
     * GET /thymeleaf/posts/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        return "posts/edit";  // templates/posts/edit.html
    }

    /**
     * 更新（POST）
     * POST /thymeleaf/posts/{id}
     *
     * 【学習ポイント】
     * HTML form は GET/POST しか送れない。PUT/DELETE は使えない。
     * そのため MPA では POST を使い、パスで操作を区別することが多い。
     *
     * 【別解: hidden フィールドで HTTP メソッドを指定】
     * Spring では _method パラメータで PUT/DELETE をエミュレートできる。
     * <input type="hidden" name="_method" value="PUT" />
     * HiddenHttpMethodFilter を有効にする必要がある。
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Post post) {
        Post existing = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        existing.setTitle(post.getTitle());
        existing.setContent(post.getContent());
        postRepository.save(existing);
        return "redirect:/thymeleaf/posts/" + id;  // 詳細ページへリダイレクト
    }

    /**
     * 削除（POST）
     * POST /thymeleaf/posts/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        postRepository.deleteById(id);
        return "redirect:/thymeleaf/posts";  // 一覧ページへリダイレクト
    }
}
