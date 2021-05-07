package cn.gasin.fs.namenode.directory.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * file-system中的一个文件夹.
 */
@Getter
public class DirectoryNode extends INode {

    private List<INode> childrenList;

    public DirectoryNode(INode parent, String path, String name) {
        super(parent, path, name);
        this.childrenList = new ArrayList<>();
    }

    public void addChildNode(INode node) {
        childrenList.add(node);
    }

    public INode getChildNode(String name) {
        // TODO 遍历childrenList, 然后返回就好了.
        return null;
    }

    public DirectoryNode getDirectoryChild(String name) {
        for (INode node : childrenList) {
            if (node instanceof DirectoryNode && node.getName().equals(name)) {
                return (DirectoryNode) node;
            }
        }
        return null;
    }

}
