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
package com.google.mu.collect;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.function.BiFunction;

import com.google.mu.function.MapFrom3;
import com.google.mu.function.MapFrom4;
import com.google.mu.function.MapFrom5;
import com.google.mu.function.MapFrom6;
import com.google.mu.function.MapFrom7;
import com.google.mu.function.MapFrom8;

/**
 * Utilities pertaining to {@link Collection}.
 *
 * @since 8.0
 */
public final class MoreCollections {
  /**
   * If {@code collection} has at least two elements, passes the first two elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findFirstElements(
      Collection<T> collection, BiFunction<? super T, ? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() < 2) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.apply(list.get(0), list.get(1)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.apply(it.next(), it.next()));
  }

  /**
   * If {@code collection} has at least 3 elements, passes the first 3 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findFirstElements(
      Collection<T> collection, MapFrom3<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() < 3) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has at least 4 elements, passes the first 4 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findFirstElements(
      Collection<T> collection, MapFrom4<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() < 4) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2), list.get(3)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has at least 5 elements, passes the first 5 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findFirstElements(
      Collection<T> collection, MapFrom5<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() < 5) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has at least 6 elements, passes the first 6 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findFirstElements(
      Collection<T> collection, MapFrom6<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() < 6) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), list.get(5)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has at least 7 elements, passes the first 6 elements to {@code found}
   * function and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   * @since 7.2
   */
  public static <T, R> Optional<R> findFirstElements(
      Collection<T> collection, MapFrom7<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() < 7) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(
          found.map(
              list.get(0),
              list.get(1),
              list.get(2),
              list.get(3),
              list.get(4),
              list.get(5),
              list.get(6)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(
        found.map(it.next(), it.next(), it.next(), it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has at least 8 elements, passes the first 6 elements to {@code found}
   * function and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   * @since 7.2
   */
  public static <T, R> Optional<R> findFirstElements(
      Collection<T> collection, MapFrom8<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() < 8) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(
          found.map(
              list.get(0),
              list.get(1),
              list.get(2),
              list.get(3),
              list.get(4),
              list.get(5),
              list.get(6),
              list.get(7)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(
        found.map(
            it.next(), it.next(), it.next(), it.next(), it.next(), it.next(), it.next(),
            it.next()));
  }

  /**
   * If {@code collection} has exactly two elements, passes the two elements to {@code found}
   * function and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findOnlyElements(
      Collection<T> collection, BiFunction<? super T, ? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() != 2) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.apply(list.get(0), list.get(1)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.apply(it.next(), it.next()));
  }

  /**
   * If {@code collection} has exactly 3 elements, passes the 3 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findOnlyElements(
      Collection<T> collection, MapFrom3<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() != 3) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has exactly 4 elements, passes the 4 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findOnlyElements(
      Collection<T> collection, MapFrom4<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() != 4) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2), list.get(3)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has exactly 5 elements, passes the 5 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findOnlyElements(
      Collection<T> collection, MapFrom5<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() != 5) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has exactly 6 elements, passes the 6 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   */
  public static <T, R> Optional<R> findOnlyElements(
      Collection<T> collection, MapFrom6<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() != 6) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(found.map(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), list.get(5)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(found.map(it.next(), it.next(), it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has exactly 7 elements, passes the 6 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   * @since 7.2
   */
  public static <T, R> Optional<R> findOnlyElements(
      Collection<T> collection, MapFrom7<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() != 7) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(
          found.map(
              list.get(0),
              list.get(1),
              list.get(2),
              list.get(3),
              list.get(4),
              list.get(5),
              list.get(6)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(
        found.map(it.next(), it.next(), it.next(), it.next(), it.next(), it.next(), it.next()));
  }

  /**
   * If {@code collection} has exactly 8 elements, passes the 6 elements to {@code found} function
   * and returns the non-null result wrapped in an {@link Optional}, or else returns {@code
   * Optional.empty()}.
   *
   * @throws NullPointerException if {@code collection} or {@code found} function is null, or if
   *     {@code found} function returns null.
   * @since 7.2
   */
  public static <T, R> Optional<R> findOnlyElements(
      Collection<T> collection, MapFrom8<? super T, ? extends R> found) {
    requireNonNull(found);
    if (collection.size() != 8) return Optional.empty();
    if (collection instanceof List && collection instanceof RandomAccess) {
      List<T> list = (List<T>) collection;
      return Optional.of(
          found.map(
              list.get(0),
              list.get(1),
              list.get(2),
              list.get(3),
              list.get(4),
              list.get(5),
              list.get(6),
              list.get(7)));
    }
    Iterator<T> it = collection.iterator();
    return Optional.of(
        found.map(
            it.next(), it.next(), it.next(), it.next(), it.next(), it.next(), it.next(),
            it.next()));
  }

  private MoreCollections() {}
}
