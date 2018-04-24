package acme.core.functions;

import javax.jcr.RepositoryException;

@FunctionalInterface
public interface RepositoryConsumer<T> {

    void accept(T t) throws RepositoryException;

}
