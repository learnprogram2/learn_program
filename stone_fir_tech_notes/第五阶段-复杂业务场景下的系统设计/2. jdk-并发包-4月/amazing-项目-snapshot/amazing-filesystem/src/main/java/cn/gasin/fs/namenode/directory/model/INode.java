package cn.gasin.fs.namenode.directory.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 代表file-system中的一个节点:
 * 最终形成一个文件树.
 */
@Setter
@Getter
public abstract class INode {
    // 文件树的根节点name和path都是null或者empty, parent是null, 其他的节点都不能是
    private String name;
    private INode parent;
    private String path;

    public INode(INode parent, String path, String name) {
        this.parent = parent;
        this.path = path;
        this.name = name;
    }


}
