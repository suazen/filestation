package me.daylight.filestation.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

/**
 * @author Daylight
 * @date 2018/12/30 19:03
 */
public class TableDto {
    private List data;

    private long count;

    public TableDto(List data, long count) {
        this.data = data;
        this.count = count;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public static class FileTable{
        private long id;
        private int type;
        private String name;
        @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
        private Date date;
        private Long size;
        private Boolean shared;

        public FileTable(long id, int type, String name, Date date, @Nullable Long size,@Nullable Boolean shared) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.date = date;
            this.size=size;
            this.shared=shared;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public Boolean getShared() {
            return shared;
        }

        public void setShared(Boolean shared) {
            this.shared = shared;
        }
    }
}
