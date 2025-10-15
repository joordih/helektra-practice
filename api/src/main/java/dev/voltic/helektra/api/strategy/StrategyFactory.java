package dev.voltic.helektra.api.strategy;

import com.google.common.collect.Maps;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("all")
public class StrategyFactory {

  private static final Map<TypeToken<?>, Strategy> strategies = Maps.newHashMap();

  public static <S extends Strategy, T> void registerStrategy(Class<S> strategyClass, Class<T> parameterClass,
      S strategy) {
    TypeToken<?> token = createTypeToken(strategyClass, parameterClass);
    strategies.put(token, strategy);
  }

  @SuppressWarnings("unchecked")
  public static <S extends Strategy, T> S getStrategy(Class<S> strategyClass, Class<T> parameterClass) {
    TypeToken<?> token = createTypeToken(strategyClass, parameterClass);
    Strategy strategy = strategies.get(token);
    if (strategy == null) {
      throw new IllegalArgumentException("No hay estrategia registrada para: " + strategyClass.getName()
          + " con parámetro " + parameterClass.getName());
    }
    return (S) strategy;
  }

  private static <S extends Strategy, T> TypeToken<?> createTypeToken(Class<S> strategyClass,
      Class<T> parameterClass) {
    return new TypeToken<S>() {
      @Override
      public boolean equals(Object o) {
        if (this == o)
          return true;
        if (o == null || getClass() != o.getClass())
          return false;
        TypeToken<?> that = (TypeToken<?>) o;
        return strategyClass.equals(that.getRawType())
            && Objects.equals(parameterClass, ((TypeToken<?>) o).getType());
      }

      @Override
      public int hashCode() {
        return Objects.hash(strategyClass, parameterClass);
      }

      @Override
      public Class<S> getRawType() {
        return strategyClass;
      }

      @Override
      public Type getType() {
        return parameterClass;
      }
    };
  }

  public static <S extends Strategy, T, R> R execute(
      Class<S> strategyClass,
      Class<T> parameterClass,
      StrategyExecutor<S, R> executor,
      Object... params) {

    S strategy = getStrategy(strategyClass, parameterClass);
    return executor.execute(strategy, params);
  }

  @FunctionalInterface
  public interface StrategyExecutor<S extends Strategy, R> {
    R execute(S strategy, Object... params);
  }

  private static abstract class TypeToken<T> {
    public abstract Class<T> getRawType();

    public abstract Type getType();
  }
}
