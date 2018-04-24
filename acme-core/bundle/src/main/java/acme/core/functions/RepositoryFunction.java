package acme.core.functions;

import javax.jcr.RepositoryException;

@FunctionalInterface
public interface RepositoryFunction<T, R> {

    R apply(T t) throws RepositoryException;
}
