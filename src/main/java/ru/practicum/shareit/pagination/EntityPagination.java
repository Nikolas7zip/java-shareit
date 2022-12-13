package ru.practicum.shareit.pagination;

public class EntityPagination {
    private int page;
    private int size;

    protected EntityPagination(int from, int size) {
        if (from < 0 || size < 1 || from % size != 0) {
            throw new IllegalArgumentException("Wrong pagination params from=" + from + ", size=" + size);
        }
        this.size = size;
        this.page = from / size;
    }

    public static EntityPagination of(int from, int size) {
        return new EntityPagination(from, size);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
