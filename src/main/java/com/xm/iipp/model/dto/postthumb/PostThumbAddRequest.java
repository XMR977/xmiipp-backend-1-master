package com.xm.iipp.model.dto.postthumb;

import java.io.Serializable;
import lombok.Data;

/**
 * 帖子点赞请求
 *
 * @author <a href="https://github.com/lixm">程序员鱼皮</a>
 * @from <a href="https://xm.icu">编程导航知识星球</a>
 */
@Data
public class PostThumbAddRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long postId;

    private static final long serialVersionUID = 1L;
}