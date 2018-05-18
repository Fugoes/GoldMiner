package util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FP {
    public static <R> Supplier<Optional<R>> liftExp(SupplierExp<R> func) {
        return () -> {
            try {
                return Optional.of(func.get());
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }

    public static <T, R> Function<T, Optional<R>> liftExp(FunctionExp<T, R> func) {
        return (arg) -> {
            try {
                return Optional.of(func.apply(arg));
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }

    public interface SupplierExp<R> {
        R get() throws Exception;
    }

    public interface FunctionExp<T, R> {
        R apply(T arg) throws Exception;
    }
}
