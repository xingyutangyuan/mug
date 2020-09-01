/*****************************************************************************
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package com.google.mu.util.stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.google.mu.util.stream.BiCollectors.toMap;
import static com.google.mu.util.stream.BiStream.toBiStream;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.google.common.truth.MultimapSubject;

@RunWith(Parameterized.class)
public class BiStreamInvariantsTest {

  @Parameter(0)
  public BiStreamFactory factory;

  @Parameter(1)
  public Variant variant;

  @Parameters(name = "{index}: {0}/{1}")
  public static ImmutableList<Object[]> factories() {
    return Sets.cartesianProduct(
            ImmutableSet.copyOf(BiStreamFactory.values()), ImmutableSet.copyOf(Variant.values()))
        .stream()
        .map(Collection::toArray)
        .collect(toImmutableList());
  }

  @Test
  public void empty() {
    assertKeyValues(of()).isEmpty();
  }

  @Test
  public void nonEmpty() {
    assertKeyValues(of("one", 1))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1))
        .inOrder();
    assertKeyValues(of("one", 1, "two", 2))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, "two", 2))
        .inOrder();
    assertKeyValues(of("one", 1, "two", 2, "three", 3))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, "two", 2, "three", 3))
        .inOrder();
  }

  @Test
  public void mapKeys() {
    assertKeyValues(of("one", 1).mapKeys((k, v) -> k + v))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one1", 1))
        .inOrder();
    assertKeyValues(of("one", 1).mapKeys(k -> k + k))
        .containsExactlyEntriesIn(ImmutableMultimap.of("oneone", 1))
        .inOrder();
    assertKeyValues(of("one", 1).mapKeys(k -> null))
        .containsExactlyEntriesIn(keyValues(null, 1))
        .inOrder();
  }

  @Test
  public void mapValues() {
    assertKeyValues(of("one", 1).mapValues((k, v) -> k + v))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", "one1"))
        .inOrder();
    assertKeyValues(of("one", 1).mapValues(v -> v * 10))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 10))
        .inOrder();
    assertKeyValues(of("one", 1).mapValues(v -> null))
        .containsExactlyEntriesIn(keyValues("one", null))
        .inOrder();
  }

  @Test
  public void innerJoinValues_found() {
    assertKeyValues(BiStream.of("uno", 1, "dos", 2).innerJoinValues(ImmutableMap.of(1, "one")))
        .containsExactly("uno", "one")
        .inOrder();
  }

  @Test
  public void innerJoinValues_notFound() {
    assertKeyValues(BiStream.of("uno", 1).innerJoinValues(ImmutableMap.of(4, "four"))).isEmpty();
  }

  @Test
  public void distinct_byKey() {
    BiStream<Integer, ?> distinct =
        BiStream.from(Stream.of(1, 1, 2, 2, 3), k -> k, x -> null).distinct();
    assertKeyValues(distinct).containsExactlyEntriesIn(keyValues(1, null, 2, null, 3, null));
  }

  @Test
  public void distinct_byValue() {
    BiStream<?, Integer> distinct =
        BiStream.from(Stream.of(1, 1, 2, 2, 3), k -> null, v -> v).distinct();
    assertKeyValues(distinct).containsExactlyEntriesIn(keyValues(null, 1, null, 2, null, 3));
  }

  @Test
  public void flatMap() {
    assertKeyValues(of("one", 1).flatMap((k, v) -> of(k, v * 10, k, v * 11)))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 10, "one", 11))
        .inOrder();
    assertKeyValues(of("one", 1).flatMap((k, v) -> of(null, null)))
        .containsExactlyEntriesIn(keyValues(null, null))
        .inOrder();
  }

  @Test
  public void flatMap_mapperReturnsNull() {
    assertKeyValues(of(1, 2, 3, 4).flatMap((k, v) -> null)).isEmpty();
  }

  @Test
  public void flatMapKeys() {
    assertKeyValues(of("one", 1).flatMapKeys((k, v) -> Stream.of(k, v)))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, 1, 1))
        .inOrder();
    assertKeyValues(of("one", 1).flatMapKeys(k -> Stream.of(k, k + k)))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, "oneone", 1))
        .inOrder();
    assertKeyValues(of("one", 1).flatMapKeys(k -> Stream.of(null, null)))
        .containsExactlyEntriesIn(keyValues(null, 1, null, 1))
        .inOrder();
  }

  @Test
  public void flatMapKeys_mapperReturnsNull() {
    assertKeyValues(of(1, 2, 3, 4).flatMapKeys(k -> null)).isEmpty();
  }

  @Test
  public void flatMapValues() {
    assertKeyValues(of("one", 1).flatMapValues((k, v) -> Stream.of(k, v)))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", "one", "one", 1))
        .inOrder();
    assertKeyValues(of("one", 1).flatMapValues(v -> Stream.of(v, v * 10)))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, "one", 10))
        .inOrder();
    assertKeyValues(of("one", 1).flatMapValues(v -> Stream.of(null, null)))
        .containsExactlyEntriesIn(keyValues("one", null, "one", null))
        .inOrder();
  }

  @Test
  public void flatMapValues_mapperReturnsNull() {
    assertKeyValues(of(1, 2, 3, 4).flatMapValues(v -> null)).isEmpty();
  }

  @Test
  public void filter() {
    assertKeyValues(of("one", 1, "two", "two").filter((k, v) -> k.equals(v)))
        .containsExactlyEntriesIn(ImmutableMultimap.of("two", "two"))
        .inOrder();
  }

  @Test
  public void filterKeys() {
    assertKeyValues(of("one", 1, "two", 2).filterKeys("one"::equals))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1))
        .inOrder();
  }

  @Test
  public void filterValues() {
    assertKeyValues(of("one", 1, "two", 2).filterValues(v -> v == 2))
        .containsExactlyEntriesIn(ImmutableMultimap.of("two", 2))
        .inOrder();
  }

  @Test
  public void append() {
    assertKeyValues(of("one", 1).append(of("two", 2, "three", 3)))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, "two", 2, "three", 3))
        .inOrder();
    assertKeyValues(of("one", 1).append("two", 2).append("three", 3))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, "two", 2, "three", 3))
        .inOrder();
    assertKeyValues(of("one", 1).append("two", null).append(null, 3))
        .containsExactlyEntriesIn(keyValues("one", 1, "two", null, null, 3))
        .inOrder();
  }

  @Test
  public void peek() {
    AtomicInteger sum = new AtomicInteger();
    assertKeyValues(of(1, 2, 3, 4).peek((k, v) -> sum.addAndGet(k + v)))
        .containsExactlyEntriesIn(ImmutableMultimap.of(1, 2, 3, 4))
        .inOrder();
    assertThat(sum.get()).isEqualTo(10);
  }

  @Test
  public void allMatch() {
    assertThat(of("one", 1, "two", 2).allMatch((k, v) -> k.equals("one") && v == 1)).isFalse();
    assertThat(of("one", 1, "two", 2).allMatch((k, v) -> k != null && v != null)).isTrue();
  }

  @Test
  public void anyMatch() {
    assertThat(of("one", 1, "two", 2).anyMatch((k, v) -> k.equals("one") && v == 1)).isTrue();
    assertThat(of("one", 1, "two", 2).anyMatch((k, v) -> k == null && v == null)).isFalse();
  }

  @Test
  public void noneMatch() {
    assertThat(of("one", 1, "two", 2).noneMatch((k, v) -> k.equals("one") && v == 1)).isFalse();
    assertThat(of("one", 1, "two", 2).noneMatch((k, v) -> k == null && v == null)).isTrue();
  }

  @Test
  public void keys() {
    assertThat(of("one", 1, "two", 2).keys()).containsExactly("one", "two").inOrder();
  }

  @Test
  public void keys_mapValuesEvaluatedToo() {
    List<Object> mappedValues = new ArrayList<>();
    assertThat(
            of("one", 1, "two", 2)
                .mapValues(
                    v -> {
                      mappedValues.add(v);
                      return v;
                    })
                .keys())
        .containsExactly("one", "two")
        .inOrder();
    assertThat(mappedValues).containsExactly(1, 2).inOrder();
  }

  @Test
  public void values() {
    assertThat(of("one", 1, "two", 2).values()).containsExactly(1, 2).inOrder();
  }

  @Test
  public void values_mapKeysEvaluatedToo() {
    List<Object> mappedKeys = new ArrayList<>();
    assertThat(
            of("one", 1, "two", 2)
                .mapKeys(
                    k -> {
                      mappedKeys.add(k);
                      return k;
                    })
                .values())
        .containsExactly(1, 2)
        .inOrder();
    assertThat(mappedKeys).containsExactly("one", "two").inOrder();
  }

  @Test
  public void testMapToObj() {
    assertThat(of("one", 1, "two", 2).mapToObj((k, v) -> k + ":" + v))
        .containsExactly("one:1", "two:2");
  }

  @Test
  public void collectToMap() {
    assertThat(of("one", 1, "two", 2).collect(toMap())).containsExactly("one", 1, "two", 2);
  }

  @Test
  public void toCollect_toMapMergingValues() {
    assertThat(of("k", 1, "k", 2).collect(toMap((v1, v2) -> v1 * 10 + v2)))
        .containsExactly("k", 12);
  }

  @Test
  public void collect() {
    Map<String, Integer> map = of("one", 1, "two", 2).collect(toMap());
    assertThat(map).containsExactly("one", 1, "two", 2);
  }

  @Test
  public void collect_toImmutableListMultimapWithInflexibleMapperTypes() {
    ImmutableListMultimap<String, Integer> multimap =
        of("one", 1, "one", 10, "two", 2).collect(new BiCollector<String, Integer, ImmutableListMultimap<String, Integer>>() {
          @Override
          public <E> Collector<E, ?, ImmutableListMultimap<String, Integer>> splitting(Function<E, String> toKey, Function<E, Integer> toValue) {
            return BiStreamInvariantsTest.toImmutableMultimap(toKey,toValue);
          }
        });
    assertThat(multimap)
        .containsExactlyEntriesIn(ImmutableListMultimap.of("one", 1, "one", 10, "two", 2));
  }

  @Test
  public void forEach() {
    AtomicInteger sum = new AtomicInteger();
    of(1, 2, 3, 4).forEach((k, v) -> sum.addAndGet(k + v));
    assertThat(sum.get()).isEqualTo(10);
  }

  @Test
  public void forEachOrdered() {
    List<Integer> list = new ArrayList<>();
    of(1, 2, 3, 4)
        .forEachOrdered(
            (k, v) -> {
              list.add(k);
              list.add(v);
            });
    assertThat(list).containsExactly(1, 2, 3, 4).inOrder();
  }

  @Test
  public void count() {
    assertThat(of(1, 2, 3, 4).count()).isEqualTo(2);
  }

  @Test
  public void map() {
    assertKeyValues(of(1, "one", 2, "two").map((k, v) -> k + ":" + v, (k, v) -> v + ":" + k))
        .containsExactly("1:one", "one:1", "2:two", "two:2")
        .inOrder();
  }

  @Test
  public void mapToObj() {
    assertThat(of(1, 2, 3, 4).mapToObj((k, v) -> k * 10 + v)).containsExactly(12, 34).inOrder();
  }

  @Test
  public void mapToInt() {
    assertThat(of(1, 2, 3, 4).mapToInt((k, v) -> k * 10 + v).boxed())
        .containsExactly(12, 34)
        .inOrder();
  }

  @Test
  public void mapToLong() {
    assertThat(of(1, 2, 3, 4).mapToLong((k, v) -> k * 10 + v).boxed())
        .containsExactly(12L, 34L)
        .inOrder();
  }

  @Test
  public void mapToDouble() {
    assertThat(of(1, 2, 3, 4).mapToDouble((k, v) -> k * 10 + v).boxed())
        .containsExactly(12D, 34D)
        .inOrder();
  }

  @Test
  public void flatMapToObj() {
    assertThat(of(1, 2, 3, 4).flatMapToObj((k, n) -> Collections.nCopies(n, k).stream()))
        .containsExactly(1, 1, 3, 3, 3, 3)
        .inOrder();
  }

  @Test
  public void flatMapToObj_mapperReturnsNull() {
    assertThat(of(1, 2, 3, 4).flatMapToObj((k, n) -> null)).isEmpty();
  }

  @Test
  public void flatMapToInt() {
    assertThat(
            of(1, 2, 3, 4)
                .flatMapToInt((k, n) -> Collections.nCopies(n, k).stream().mapToInt(i -> i))
                .boxed())
        .containsExactly(1, 1, 3, 3, 3, 3)
        .inOrder();
  }

  @Test
  public void flatMapToInt_mapperReturnsNull() {
    assertThat(of(1, 2, 3, 4).flatMapToInt((k, n) -> null)).isEmpty();
  }

  @Test
  public void flatMapToLong() {
    assertThat(
            of(1, 2, 3, 4)
                .flatMapToLong((k, n) -> Collections.nCopies(n, k).stream().mapToLong(i -> i))
                .boxed())
        .containsExactly(1L, 1L, 3L, 3L, 3L, 3L)
        .inOrder();
  }

  @Test
  public void flatMapToLong_mapperReturnsNull() {
    assertThat(of(1, 2, 3, 4).flatMapToLong((k, n) -> null)).isEmpty();
  }

  @Test
  public void flatMapToDouble() {
    assertThat(
            of(1, 2, 3, 4)
                .flatMapToDouble((k, n) -> Collections.nCopies(n, k).stream().mapToDouble(i -> i))
                .boxed())
        .containsExactly(1D, 1D, 3D, 3D, 3D, 3D)
        .inOrder();
  }

  @Test
  public void flatMapToDouble_mapperReturnsNull() {
    assertThat(of(1, 2, 3, 4).flatMapToDouble((k, v) -> null).boxed()).isEmpty();
  }

  @Test
  public void limit() {
    assertKeyValues(of("one", 1, "two", 2, "three", 3).limit(2))
        .containsExactlyEntriesIn(ImmutableMultimap.of("one", 1, "two", 2))
        .inOrder();
  }

  @Test
  public void skip() {
    assertKeyValues(of("one", 1, "two", 2, "three", 3).skip(1))
        .containsExactlyEntriesIn(ImmutableMultimap.of("two", 2, "three", 3))
        .inOrder();
  }

  @Test
  public void sortedByKeys() {
    assertKeyValues(of("a", 1, "c", 2, "b", 3).sortedByKeys(Comparator.naturalOrder()))
        .containsExactlyEntriesIn(ImmutableMultimap.of("a", 1, "b", 3, "c", 2))
        .inOrder();
  }

  @Test
  public void sortedByValues() {
    assertKeyValues(of("a", 3, "b", 1, "c", 2).sortedByValues(Comparator.naturalOrder()))
        .containsExactlyEntriesIn(ImmutableMultimap.of("b", 1, "c", 2, "a", 3))
        .inOrder();
  }

  @Test
  public void distinct() {
    assertKeyValues(of("a", 1, "b", 2, "a", 1).distinct())
        .containsExactlyEntriesIn(ImmutableMultimap.of("a", 1, "b", 2))
        .inOrder();
    assertKeyValues(of("a", null, "a", null).distinct())
        .containsExactlyEntriesIn(keyValues("a", null))
        .inOrder();
    assertKeyValues(of("a", null, "b", null).distinct())
        .containsExactlyEntriesIn(keyValues("a", null, "b", null))
        .inOrder();
    assertKeyValues(of(null, 1, null, 1).distinct())
        .containsExactlyEntriesIn(keyValues(null, 1))
        .inOrder();
    assertKeyValues(of(null, 1, null, 2).distinct())
        .containsExactlyEntriesIn(keyValues(null, 1, null, 2))
        .inOrder();
    assertKeyValues(of(null, null, null, null).distinct())
        .containsExactlyEntriesIn(keyValues(null, null))
        .inOrder();
  }

  @Test
  public void inverse() {
    assertKeyValues(of("a", 1).inverse())
        .containsExactlyEntriesIn(ImmutableMultimap.of(1, "a"))
        .inOrder();
  }

  private <K, V> BiStream<K, V> of() {
    return variant.wrap(factory.newBiStream());
  }

  private <K, V> BiStream<K, V> of(K key, V value) {
    return variant.wrap(factory.newBiStream(key, value));
  }

  private <K, V> BiStream<K, V> of(K k1, V v1, K k2, V v2) {
    return variant.wrap(factory.newBiStream(k1, v1, k2, v2));
  }

  private <K, V> BiStream<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
    return variant.wrap(factory.newBiStream(k1, v1, k2, v2, k3, v3));
  }

  static<K, V> MultimapSubject assertKeyValues(BiStream<K, V> stream) {
    Multimap<?, ?> multimap = stream.collect(new BiCollector<K, V, Multimap<K,V>>() {
      @Override
      public <E> Collector<E, ?, Multimap<K, V>> splitting(Function<E, K> toKey, Function<E, V> toValue) {
        return BiStreamInvariantsTest.toLinkedListMultimap(toKey,toValue);
      }
    });
    return assertThat(multimap);
  }

  // Intentionally declare the parameter types without wildcards, to make sure
  // BiCollector can still work with such naive method references.
  public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> toImmutableMultimap(
      Function<T, K> keyMapper, Function<T, V> valueMapper) {
    return ImmutableListMultimap.toImmutableListMultimap(keyMapper, valueMapper);
  }

  private static <T, K, V> Collector<T, ?, Multimap<K, V>> toLinkedListMultimap(
      Function<? super T, ? extends K> toKey, Function<? super T, ? extends V> toValue) {
    return Collector.of(
        LinkedListMultimap::create,
        (m, e) -> m.put(toKey.apply(e), toValue.apply(e)),
        (m1, m2) -> {
          m1.putAll(m2);
          return m1;
        });
  }

  private static <K, V> ListMultimap<K, V> keyValues(K key, V value) {
    ListMultimap<K, V> multimap = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    multimap.put(key, value);
    return multimap;
  }

  private static <K, V> ListMultimap<K, V> keyValues(K k1, V v1, K k2, V v2) {
    ListMultimap<K, V> multimap = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    multimap.put(k1, v1);
    multimap.put(k2, v2);
    return multimap;
  }

  private static <K, V> ListMultimap<K, V> keyValues(K k1, V v1, K k2, V v2, K k3, V v3) {
    ListMultimap<K, V> multimap = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    multimap.put(k1, v1);
    multimap.put(k2, v2);
    multimap.put(k3, v3);
    return multimap;
  }

  /** Different ways of creating a logical {@BiStream}. */
  private enum BiStreamFactory {
    DEFAULT {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return BiStream.empty();
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return BiStream.of(key, value);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return BiStream.of(k1, v1, k2, v2);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return BiStream.of(k1, v1, k2, v2, k3, v3);
      }
    },
    FROM_BUILDER {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return BiStream.<K, V>builder().build();
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return BiStream.<K, V>builder().add(key, value).build();
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return BiStream.<K, V>builder().add(k1, v1).add(k2, v2).build();
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return BiStream.<K, V>builder().add(k1, v1).add(k2, v2).add(k3, v3).build();
      }
    },
    FROM_GENERIC_ENTRY_STREAM {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return BiStream.from(
            ImmutableListMultimap.<K, V>of().entries().stream(),
            Map.Entry::getKey,
            Map.Entry::getValue);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return BiStream.from(
            keyValues(value, key).entries().stream(), Map.Entry::getValue, Map.Entry::getKey);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return BiStream.from(
            keyValues(k1, v1, k2, v2).entries().stream(), Map.Entry::getKey, Map.Entry::getValue);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return BiStream.from(
            keyValues(k1, v1, k2, v2, k3, v3).entries().stream(),
            Map.Entry::getKey,
            Map.Entry::getValue);
      }
    },
    COLLECTED_FROM_GENERIC_ENTRY_STREAM {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return ImmutableListMultimap.<K, V>of().entries().stream()
            .collect(toBiStream(Map.Entry::getKey, Map.Entry::getValue));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return keyValues(value, key).entries().stream()
            .collect(toBiStream(Map.Entry::getValue, Map.Entry::getKey));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return keyValues(k1, v1, k2, v2).entries().stream()
            .collect(toBiStream(Map.Entry::getKey, Map.Entry::getValue));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return keyValues(k1, v1, k2, v2, k3, v3).entries().stream()
            .collect(toBiStream(Map.Entry::getKey, Map.Entry::getValue));
      }
    },
    FROM_DUAL_VALUED_FUNCTION {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return BiStream.from(
            ImmutableListMultimap.<K, V>of().entries().stream(), this::fromEntry);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return BiStream.from(
            keyValues(key, value).entries().stream(), this::fromEntry);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return BiStream.from(
            keyValues(k1, v1, k2, v2).entries().stream(), this::fromEntry);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return BiStream.from(
            keyValues(k1, v1, k2, v2, k3, v3).entries().stream(), this::fromEntry);
      }

      private <K, V, R> R fromEntry(Map.Entry<K, V> entry, BiFunction<K, V, R> joiner) {
        return joiner.apply(entry.getKey(), entry.getValue());
      }
    },
    COLLECTED_FROM_DUAL_VALUED_FUNCTION {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return ImmutableListMultimap.<K, V>of().entries().stream()
            .collect(toBiStream(this::fromEntry));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return keyValues(key, value).entries().stream()
            .collect(toBiStream(this::fromEntry));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return keyValues(k1, v1, k2, v2).entries().stream()
            .collect(toBiStream(this::fromEntry));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return keyValues(k1, v1, k2, v2, k3, v3).entries().stream()
            .collect(toBiStream(this::fromEntry));
      }

      private <K, V, R> R fromEntry(Map.Entry<K, V> entry, BiFunction<K, V, R> joiner) {
        return joiner.apply(entry.getKey(), entry.getValue());
      }
    },
    FROM_COLLECTION {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return BiStream.from(
            ImmutableListMultimap.<K, V>of().entries(), Map.Entry::getKey, Map.Entry::getValue);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return BiStream.from(
            keyValues(value, key).entries(), Map.Entry::getValue, Map.Entry::getKey);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return BiStream.from(
            keyValues(k1, v1, k2, v2).entries(), Map.Entry::getKey, Map.Entry::getValue);
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return BiStream.from(
            keyValues(k1, v1, k2, v2, k3, v3).entries(), Map.Entry::getKey, Map.Entry::getValue);
      }
    },
    FROM_ZIP {
      @Override
      <K, V> BiStream<K, V> newBiStream() {
        return BiStream.zip(ImmutableList.of(), ImmutableList.of());
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K key, V value) {
        return BiStream.zip(asList(key), asList(value));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2) {
        return BiStream.zip(asList(k1, k2), asList(v1, v2));
      }

      @Override
      <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3) {
        return BiStream.zip(Stream.of(k1, k2, k3), Stream.of(v1, v2, v3));
      }
    },
    ;

    abstract <K, V> BiStream<K, V> newBiStream();

    abstract <K, V> BiStream<K, V> newBiStream(K key, V value);

    abstract <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2);

    abstract <K, V> BiStream<K, V> newBiStream(K k1, V v1, K k2, V v2, K k3, V v3);
  }

  /** Different variants of a logically equivalent {@code BiStream}. */
  private enum Variant {
    DEFAULT {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream;
      }
    },
    PARALLEL_THEN_SEQUENTIAL {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return BiStream.from(stream.mapToEntry().parallel().sequential());
      }
    },
    INVERSE_OF_INVERSE {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.inverse().inverse();
      }
    },
    VERY_LARGE_LIMIT {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.limit(Integer.MAX_VALUE);
      }
    },
    SKIP_ZERO {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.skip(0);
      }
    },
    MAP_KEY_TO_SELF {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.mapKeys(identity());
      }
    },
    MAP_VALUE_TO_SELF {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.mapValues(identity());
      }
    },
    MAP_KEY_AND_VALUE_TO_SELF {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.map((k, v) -> k, (k, v) -> v);
      }
    },
    TRIVIAL_FILTER {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.filter((k, v) -> true);
      }
    },
    TRIVIAL_FILTER_KEY {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.filterKeys(k -> true);
      }
    },
    TRIVIAL_FILTER_VALUE {
      @Override
      <K, V> BiStream<K, V> wrap(BiStream<K, V> stream) {
        return stream.filterValues(k -> true);
      }
    },
    ;

    abstract <K, V> BiStream<K, V> wrap(BiStream<K, V> stream);
  }
}
