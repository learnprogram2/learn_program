package cn.gasin.fs.namenode.directory;

import cn.gasin.fs.namenode.directory.model.DirectoryNode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * fs的目录维护.
 */
public class FSDirectory {

    @Getter
    private DirectoryNode root;

    public FSDirectory() {
        root = new DirectoryNode(null, "/", "/");
    }

    /**
     * create directory
     */
    public void mkdir(String path) throws Exception {
        synchronized (root) { // 简单的控制并发.

            String[] paths = verityPath(path);

            // 从根目录, 一直DFS遍历到最底层文件夹
            DirectoryNode parent = root;
            StringBuilder currentPath = new StringBuilder();
            for (String name : paths) {
                DirectoryNode currentNode = parent.getDirectoryChild(name);
                if (Objects.isNull(currentNode)) {
                    // create currentNode and set into next;
                    currentNode = new DirectoryNode(parent, currentPath.append("/").append(name).toString(), name);
                    parent.addChildNode(currentNode);
                }
                parent = currentNode;
            }
        }
    }

    // 从paths截取每一层的name
    private String[] verityPath(String path) throws Exception {
        String[] paths = path.split("/");
        if (paths[0].isEmpty()) {
            paths = Arrays.copyOfRange(paths, 1, paths.length);
        }
        if (paths.length == 0) {
            throw new Exception("path can not empty.");
        }
        for (String s : paths) {
            if (s.length() == 0) {
                throw new Exception("path not verified.");
            }
        }
        return paths;
    }
}
