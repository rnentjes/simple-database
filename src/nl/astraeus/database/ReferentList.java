package nl.astraeus.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Date: 11/16/13
 * Time: 9:26 PM
 */
public class ReferentList<M> implements List<M> {

    private Class<M> cls;
    private List<Long> idList = new ArrayList<>();
    private MetaData<M> meta;
    private Map<Long, M> incoming;

    public ReferentList(Class<M> cls, MetaData<M> meta) {
        this.cls = cls;
        this.meta = meta;
    }

    public Map<Long, M> getIncoming() {
        if (incoming == null) {
            incoming = new HashMap<>();
        }

        return incoming;
    }

    public void clearIncoming() {
        getIncoming().clear();
    }

    public int size() {
        return idList.size();
    }

    public boolean isEmpty() {
        return idList.isEmpty();
    }

    public boolean contains(Object o) {
        return o.getClass().equals(cls) && idList.contains(meta.getId(o));
    }

    public List<Long> getIdList() {
        return idList;
    }

    public Class<M> getType() {
        return cls;
    }

    public Iterator<M> iterator() {
        return new Iterator<M>() {
            Iterator<Long> it = idList.iterator();
            M next = null;

            public boolean hasNext() {
                while (next == null && it.hasNext()) {
                    Long id = it.next();

                    next = meta.find(id);
                }

                return (next != null);
            }

            public M next() {
                M result = next;

                next = null;

                while (it.hasNext() && next == null) {
                    Long nextId = it.next();

                    next = getIncoming().get(nextId);

                    if (next == null) {
                        next = meta.find(nextId);
                    }
                }

                return result;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Object[] toArray() {
        throw new IllegalStateException("Not implemented yet.");
    }

    public <T> T[] toArray(T[] a) {
        throw new IllegalStateException("Not implemented yet.");
    }

    public boolean add(M m) {
        //SimpleStore.get().assertIsStored(m);

        getIncoming().put(meta.getId(m), m);

        return idList.add(meta.getId(m));
    }

    // used by SimplePersistence to copy this list,
    // using this function will avoid the entity to be added to the incoming list
    public boolean addId(Long id) {
        return idList.add(id);
    }

    public boolean remove(Object o) {
        getIncoming().remove(meta.getId(o));
        return idList.remove(meta.getId(o));
    }

    public boolean containsAll(Collection<?> c) {
        throw new IllegalStateException("Not implemented yet.");
    }

    public boolean addAll(Collection<? extends M> c) {
        boolean result = true;

        for (M m : c) {
            result = result && add(m);
        }

        return result;
    }

    public boolean addAll(int index, Collection<? extends M> c) {
        for (M m : c) {
            add(index, m);
        }

        return true;
    }

    public boolean removeAll(Collection<?> c) {
        for (Object m : c) {
            remove(m);
        }

        return true;
    }

    private List<Long> getIdList(Collection<? extends M> c) {
        List <Long> result = new LinkedList<>();

        for (M m : c) {
            result.add(meta.getId(m));
        }

        return result;
    }

    public boolean retainAll(Collection<?> c) {
        return idList.retainAll(getIdList((Collection<? extends M>) c));
    }

    public void clear() {
        getIncoming().clear();
        idList.clear();
    }

    public M get(int index) {
        Long id = idList.get(index);

        M result = getIncoming().get(id);

        if (result == null) {
            result = meta.find(id);
        }

        return result;
    }

    public M set(int index, M element) {
        M result = get(index);

        getIncoming().put(meta.getId(element), element);

        idList.set(index, meta.getId(element));

        return result;
    }

    public void add(int index, M element) {
        getIncoming().put(meta.getId(element), element);

        idList.add(index, meta.getId(element));
    }

    public M remove(int index) {
        Long id = idList.remove(index);

        M result = getIncoming().get(id);

        if (result == null) {
            result = meta.find(id);
        }

        return result;
    }

    public int indexOf(Object o) {
        return idList.indexOf(meta.getId(o));
    }

    public int lastIndexOf(Object o) {
        return idList.lastIndexOf(meta.getId(o));
    }

    public ListIterator<M> listIterator() {
        throw new IllegalStateException("Not implemented yet.");
    }

    public ListIterator<M> listIterator(int index) {
        throw new IllegalStateException("Not implemented yet.");
    }

    public List<M> subList(int fromIndex, int toIndex) {
        throw new IllegalStateException("Not implemented yet.");
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        if (idList.isEmpty()) {
            result.append("empty");
        } else {
            for (int i = 0; i < 10 && this.idList.size() > i; i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(this.idList.get(i));
            }
            if (this.idList.size() > 10) {
                result.append(", <" + (this.idList.size() - 10) + " more>");
            }
        }
        return result.toString();
    }
}
