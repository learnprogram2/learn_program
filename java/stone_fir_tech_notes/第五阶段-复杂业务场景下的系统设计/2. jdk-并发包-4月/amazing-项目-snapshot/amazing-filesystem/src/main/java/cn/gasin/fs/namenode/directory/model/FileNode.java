package cn.gasin.fs.namenode.directory.model;

import lombok.Getter;

/**
 * file-system树中的一个文件.
 */
@Getter
public class FileNode extends INode {

    public FileNode(DirectoryNode parent, String path, String name) {
        super(parent, path, name);
    }

}
