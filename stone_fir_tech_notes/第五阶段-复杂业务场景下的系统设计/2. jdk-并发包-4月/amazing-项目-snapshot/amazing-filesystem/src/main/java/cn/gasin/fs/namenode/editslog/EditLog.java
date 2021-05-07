package cn.gasin.fs.namenode.editslog;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * 一条log
 */
@Log4j2
@Getter
public class EditLog {
    private Long txid;
    private String content;

    public EditLog(long txid, String content) {
        this.txid = txid;
        this.content = content;
    }
}
