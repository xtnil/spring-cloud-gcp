/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spring.data.datastore.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Datastore.TransactionCallable;
import com.google.cloud.datastore.DatastoreReaderWriter;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.GqlQuery;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.KeyQuery;
import com.google.cloud.datastore.KeyValue;
import com.google.cloud.datastore.ListValue;
import com.google.cloud.datastore.LongValue;
import com.google.cloud.datastore.NullValue;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.Query.ResultType;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.spring.core.util.MapBuilder;
import com.google.cloud.spring.data.datastore.core.convert.DatastoreEntityConverter;
import com.google.cloud.spring.data.datastore.core.convert.ObjectToKeyFactory;
import com.google.cloud.spring.data.datastore.core.convert.ReadWriteConversions;
import com.google.cloud.spring.data.datastore.core.mapping.DatastoreDataException;
import com.google.cloud.spring.data.datastore.core.mapping.DatastoreMappingContext;
import com.google.cloud.spring.data.datastore.core.mapping.DatastorePersistentEntity;
import com.google.cloud.spring.data.datastore.core.mapping.Descendants;
import com.google.cloud.spring.data.datastore.core.mapping.DiscriminatorField;
import com.google.cloud.spring.data.datastore.core.mapping.DiscriminatorValue;
import com.google.cloud.spring.data.datastore.core.mapping.Field;
import com.google.cloud.spring.data.datastore.core.mapping.LazyReference;
import com.google.cloud.spring.data.datastore.core.mapping.event.AfterDeleteEvent;
import com.google.cloud.spring.data.datastore.core.mapping.event.AfterFindByKeyEvent;
import com.google.cloud.spring.data.datastore.core.mapping.event.AfterQueryEvent;
import com.google.cloud.spring.data.datastore.core.mapping.event.AfterSaveEvent;
import com.google.cloud.spring.data.datastore.core.mapping.event.BeforeDeleteEvent;
import com.google.cloud.spring.data.datastore.core.mapping.event.BeforeSaveEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.TypeInformation;

/** Tests for the Datastore Template. */
class DatastoreTemplateTests {

  private final Datastore datastore = mock(Datastore.class);
  private final DatastoreEntityConverter datastoreEntityConverter =
      mock(DatastoreEntityConverter.class);
  private final ObjectToKeyFactory objectToKeyFactory = mock(ObjectToKeyFactory.class);
  private final ReadWriteConversions readWriteConversions = mock(ReadWriteConversions.class);
  // A fake entity query used for testing.
  private final Query testEntityQuery =
      GqlQuery.newGqlQueryBuilder(ResultType.PROJECTION_ENTITY, "fake query").build();
  // This is the query that is expected to be constructed by the template.
  private final Query findAllTestEntityQuery =
      Query.newEntityQueryBuilder().setKind("custom_test_kind").build();
  // The keys, entities, and objects below are constructed for all tests below. the
  // number of each
  // object here corresponds to the same thing across keys, entities, objects.
  private final Key key1 = createFakeKey("key1");
  private final Key key2 = createFakeKey("key2");
  private final Key keyChild1 = createFakeKey("key3");
  private final Key badKey = createFakeKey("badkey");
  private final Entity e1 =
      Entity.newBuilder(this.key1)
          .set("singularReference", this.keyChild1)
          .set("multipleReference", Collections.singletonList(KeyValue.of(this.keyChild1)))
          .build();
  private final Entity e2 =
      Entity.newBuilder(this.key2)
          .set("singularReference", this.keyChild1)
          .set("multipleReference", Collections.singletonList(KeyValue.of(this.keyChild1)))
          .build();

  private DatastoreTemplate datastoreTemplate;
  private ChildEntity childEntity2;
  private ChildEntity childEntity3;
  private ChildEntity childEntity4;
  private ChildEntity childEntity5;
  private ChildEntity childEntity6;
  private ChildEntity childEntity7;
  private Key childKey2;
  private Key childKey3;
  private Key childKey4;
  private Key childKey5;
  private Key childKey6;
  private Key childKey7;
  private SimpleTestEntity simpleTestEntity = new SimpleTestEntity();
  private SimpleTestEntity simpleTestEntityNullVallues = new SimpleTestEntity();
  private TestEntity ob1;
  private TestEntity ob2;
  private ChildEntity childEntity1;

  private Key createFakeKey(String val) {
    return new KeyFactory("project").setKind("custom_test_kind").newKey(val);
  }

  @BeforeEach
  void setup() {
    this.datastoreTemplate =
        new DatastoreTemplate(
            () -> this.datastore,
            this.datastoreEntityConverter,
            new DatastoreMappingContext(),
            this.objectToKeyFactory);

    when(this.datastoreEntityConverter.getConversions()).thenReturn(this.readWriteConversions);

    // The readWriteConversions are only mocked for purposes of collection-conversion
    // for
    // descendants. no other conversions take place in the template.
    doAnswer(invocation -> new LinkedList<>(invocation.getArgument(0)))
        .when(this.readWriteConversions)
        .convertOnRead(any(), any(), (Class) any());

    this.ob1 = new TestEntity();
    this.ob2 = new TestEntity();

    this.ob1.id = "value1";
    this.ob2.id = "value2";

    Entity ce1 = Entity.newBuilder(this.keyChild1).build();

    Query childTestEntityQuery =
        Query.newEntityQueryBuilder()
            .setKind("child_entity")
            .setFilter(PropertyFilter.hasAncestor(this.key1))
            .build();

    this.childEntity1 = createChildEntity();

    this.ob1.childEntities = new LinkedList<>();
    this.childEntity2 = new ChildEntity();
    this.ob1.childEntities.add(this.childEntity2);

    this.childEntity3 = new ChildEntity();
    this.ob1.childEntities.add(this.childEntity3);

    this.childEntity4 = new ChildEntity();
    this.ob1.singularReference = this.childEntity4;

    this.ob1.multipleReference = new LinkedList<>();
    this.childEntity5 = new ChildEntity();
    this.ob1.multipleReference.add(this.childEntity5);

    this.childEntity6 = new ChildEntity();
    this.ob1.multipleReference.add(this.childEntity6);

    this.ob1.lazyMultipleReference = new LinkedList<>();

    this.childEntity7 = new ChildEntity();
    this.ob1.lazyMultipleReference.add(this.childEntity7);

    // mocked query results for entities and child entities.
    QueryResults childTestEntityQueryResults = mock(QueryResults.class);
    doAnswer(
            invocation -> {
              Arrays.asList(ce1).iterator().forEachRemaining(invocation.getArgument(0));
              return null;
            })
        .when(childTestEntityQueryResults)
        .forEachRemaining(any());

    QueryResults testEntityQueryResults = mock(QueryResults.class);
    doAnswer(
            invocation -> {
              Arrays.asList(this.e1, this.e2)
                  .iterator()
                  .forEachRemaining(invocation.getArgument(0));
              return null;
            })
        .when(testEntityQueryResults)
        .forEachRemaining(any());
    setUpConverters(ce1, childTestEntityQuery, childTestEntityQueryResults, testEntityQueryResults);
  }

  private ChildEntity createChildEntity() {
    ChildEntity e = new ChildEntity();
    e.id = createFakeKey("key3");
    return e;
  }

  private void setUpConverters(
      Entity ce1,
      Query childTestEntityQuery,
      QueryResults childTestEntityQueryResults,
      QueryResults testEntityQueryResults) {
    // mocking the converter to return the final objects corresponding to their
    // specific entities.
    DatastorePersistentEntity testPersistentEntity =
        new DatastoreMappingContext().getDatastorePersistentEntity(TestEntity.class);
    DatastorePersistentEntity childPersistentEntity =
        new DatastoreMappingContext().getDatastorePersistentEntity(ChildEntity.class);
    when(this.datastoreEntityConverter.read(TestEntity.class, this.e1)).thenReturn(this.ob1);
    when(this.datastoreEntityConverter.getDiscriminationPersistentEntity(TestEntity.class, this.e1))
        .thenReturn(testPersistentEntity);
    when(this.datastoreEntityConverter.read(TestEntity.class, this.e2)).thenReturn(this.ob2);
    when(this.datastoreEntityConverter.getDiscriminationPersistentEntity(TestEntity.class, this.e2))
        .thenReturn(testPersistentEntity);
    when(this.datastoreEntityConverter.read(eq(ChildEntity.class), same(ce1)))
        .thenAnswer(invocationOnMock -> createChildEntity());
    when(this.datastoreEntityConverter.getDiscriminationPersistentEntity(
            eq(ChildEntity.class), same(ce1)))
        .thenReturn(childPersistentEntity);

    doAnswer(
            invocation -> {
              FullEntity.Builder builder = invocation.getArgument(1);
              builder.set("color", "simple_test_color");
              builder.set("int_field", 1);
              return null;
            })
        .when(this.datastoreEntityConverter)
        .write(same(this.simpleTestEntity), any());

    doAnswer(
            invocation -> {
              FullEntity.Builder builder = invocation.getArgument(1);
              builder.set("color", NullValue.of());
              builder.set("int_field", NullValue.of());
              return null;
            })
        .when(this.datastoreEntityConverter)
        .write(same(this.simpleTestEntityNullVallues), any());

    when(this.datastore.run(this.testEntityQuery)).thenReturn(testEntityQueryResults);
    when(this.datastore.run(this.findAllTestEntityQuery)).thenReturn(testEntityQueryResults);
    when(this.datastore.run(childTestEntityQuery)).thenReturn(childTestEntityQueryResults);

    // Because get() takes varags, there is difficulty in matching the single param
    // case using just thenReturn.
    doAnswer(
            invocation -> {
              Object key = invocation.getArgument(0);
              List<Entity> result = new ArrayList<>();
              if (key instanceof Key) {
                if (key == this.key1) {
                  result.add(this.e1);
                } else if (key == this.keyChild1) {
                  result.add(ce1);
                } else {
                  result.add(null);
                }
              }
              return result;
            })
        .when(this.datastore)
        .fetch((Key[]) any());

    when(this.objectToKeyFactory.getKeyFromId(eq(this.key1), any())).thenReturn(this.key1);
    when(this.objectToKeyFactory.getKeyFromId(eq(this.key2), any())).thenReturn(this.key2);
    when(this.objectToKeyFactory.getKeyFromId(eq(this.keyChild1), any()))
        .thenReturn(this.keyChild1);
    when(this.objectToKeyFactory.getKeyFromId(eq(this.badKey), any())).thenReturn(this.badKey);

    when(this.objectToKeyFactory.getKeyFromObject(eq(this.ob1), any())).thenReturn(this.key1);
    when(this.objectToKeyFactory.getKeyFromObject(eq(this.ob2), any())).thenReturn(this.key2);
    this.childKey2 = createFakeKey("child_id2");
    when(this.objectToKeyFactory.allocateKeyForObject(
            same(this.childEntity2), any(), eq(this.key1)))
        .thenReturn(this.childKey2);
    this.childKey3 = createFakeKey("child_id3");
    when(this.objectToKeyFactory.allocateKeyForObject(
            same(this.childEntity3), any(), eq(this.key1)))
        .thenReturn(this.childKey3);
    this.childKey4 = createFakeKey("child_id4");
    when(this.objectToKeyFactory.allocateKeyForObject(same(this.childEntity4), any(), any()))
        .thenReturn(this.childKey4);
    when(this.objectToKeyFactory.getKeyFromObject(same(this.childEntity4), any()))
        .thenReturn(this.childKey4);
    this.childKey5 = createFakeKey("child_id5");
    when(this.objectToKeyFactory.allocateKeyForObject(same(this.childEntity5), any(), any()))
        .thenReturn(this.childKey5);
    when(this.objectToKeyFactory.getKeyFromObject(same(this.childEntity5), any()))
        .thenReturn(this.childKey5);
    this.childKey6 = createFakeKey("child_id6");
    when(this.objectToKeyFactory.allocateKeyForObject(same(this.childEntity6), any(), any()))
        .thenReturn(this.childKey6);
    when(this.objectToKeyFactory.getKeyFromObject(same(this.childEntity6), any()))
        .thenReturn(this.childKey6);
    this.childKey7 = createFakeKey("child_id7");
    when(this.objectToKeyFactory.allocateKeyForObject(same(this.childEntity7), any(), any()))
        .thenReturn(this.childKey7);
    when(this.objectToKeyFactory.getKeyFromObject(same(this.childEntity7), any()))
        .thenReturn(this.childKey7);
  }

  @Test
  void multipleNamespaceTest() {
    Datastore databaseClient1 = mock(Datastore.class);
    Datastore databaseClient2 = mock(Datastore.class);

    AtomicInteger currentClient = new AtomicInteger(1);

    Supplier<Integer> regionProvider = currentClient::getAndIncrement;

    // this client selector will alternate between the two clients
    ConcurrentHashMap<Integer, Datastore> store = new ConcurrentHashMap<>();
    Supplier<Datastore> clientProvider =
        () ->
            store.computeIfAbsent(
                regionProvider.get(), u -> u % 2 == 1 ? databaseClient1 : databaseClient2);

    DatastoreTemplate template =
        new DatastoreTemplate(
            clientProvider,
            this.datastoreEntityConverter,
            new DatastoreMappingContext(),
            this.objectToKeyFactory);

    ChildEntity childEntity = new ChildEntity();
    childEntity.id = createFakeKey("key");

    when(this.objectToKeyFactory.getKeyFromObject(same(childEntity), any()))
        .thenReturn(childEntity.id);

    // this first save should use the first client
    template.save(childEntity);
    verify(databaseClient1, times(1)).put((FullEntity<?>[]) any());
    verify(databaseClient2, times(0)).put((FullEntity<?>[]) any());

    // this second save should use the second client
    template.save(childEntity);
    verify(databaseClient1, times(1)).put((FullEntity<?>[]) any());
    verify(databaseClient2, times(1)).put((FullEntity<?>[]) any());

    // this third save should use the first client again
    template.save(childEntity);
    verify(databaseClient1, times(2)).put((FullEntity<?>[]) any());
    verify(databaseClient2, times(1)).put((FullEntity<?>[]) any());
  }

  @Test
  void performTransactionTest() {

    DatastoreReaderWriter transactionContext = mock(DatastoreReaderWriter.class);

    when(this.datastore.runInTransaction(any()))
        .thenAnswer(
            invocation -> {
              TransactionCallable<String> callable = invocation.getArgument(0);
              return callable.run(transactionContext);
            });

    List<Entity> e1 = Collections.singletonList(this.e1);
    when(transactionContext.fetch(ArgumentMatchers.<Key[]>any())).thenReturn(e1);

    String finalResult =
        this.datastoreTemplate.performTransaction(
            datastoreOperations -> {
              datastoreOperations.save(this.ob2);
              datastoreOperations.findById("ignored", TestEntity.class);
              return "all done";
            });

    assertThat(finalResult).isEqualTo("all done");
    verify(transactionContext, times(1)).put(ArgumentMatchers.<FullEntity[]>any());
    verify(transactionContext, times(2)).fetch((Key[]) any());
  }

  @Test
  void findAllByIdTestNotNull() {
    assertThat(
            this.datastoreTemplate.findAllById(
                Collections.singletonList(this.badKey), TestEntity.class))
        .isEmpty();
  }

  @Test
  void findByIdTest() {
    verifyBeforeAndAfterEvents(
        null,
        new AfterFindByKeyEvent(
            Collections.singletonList(this.ob1), Collections.singleton(this.key1)),
        () -> {
          TestEntity result = this.datastoreTemplate.findById(this.key1, TestEntity.class);
          assertThat(result).isEqualTo(this.ob1);
          assertThat(result.childEntities).contains(this.childEntity1);
          assertThat(this.childEntity1).isEqualTo(result.singularReference);
          assertThat(result.multipleReference).contains(this.childEntity1);
        },
        x -> {});
  }

  @Test
  void findByIdNotFoundTest() {
    when(this.datastore.fetch(ArgumentMatchers.<Key[]>any()))
        .thenReturn(Collections.singletonList(null));
    verifyBeforeAndAfterEvents(
        null,
        new AfterFindByKeyEvent(Collections.emptyList(), Collections.singleton(null)),
        () ->
            assertThat(this.datastoreTemplate.findById(createFakeKey("key0"), TestEntity.class))
                .isNull(),
        x -> {});
  }

  @Test
  void findAllByIdTest() {
    when(this.datastore.fetch(this.key2, this.key1)).thenReturn(Arrays.asList(this.e1, this.e2));
    List<Key> keys = Arrays.asList(this.key1, this.key2);

    verifyBeforeAndAfterEvents(
        null,
        new AfterFindByKeyEvent(Arrays.asList(this.ob1, this.ob2), new HashSet<>(keys)),
        () ->
            assertThat(this.datastoreTemplate.findAllById(keys, TestEntity.class))
                .containsExactly(this.ob1, this.ob2),
        x -> {});
  }

  @Test
  void findAllByIdReferenceConsistencyTest() {
    when(this.objectToKeyFactory.getKeyFromObject(eq(this.childEntity1), any()))
        .thenReturn(this.childEntity1.id);

    when(this.datastore.fetch(this.key1)).thenReturn(Collections.singletonList(this.e1));

    verifyBeforeAndAfterEvents(
        null,
        new AfterFindByKeyEvent(
            Collections.singletonList(this.ob1), Collections.singleton(this.key1)),
        () -> {
          TestEntity parentEntity1 = this.datastoreTemplate.findById(this.key1, TestEntity.class);
          assertThat(parentEntity1).isSameAs(this.ob1);
          ChildEntity singularReference1 = parentEntity1.singularReference;
          ChildEntity childEntity1 = parentEntity1.childEntities.get(0);
          assertThat(singularReference1).isSameAs(childEntity1);

          TestEntity parentEntity2 = this.datastoreTemplate.findById(this.key1, TestEntity.class);
          assertThat(parentEntity2).isSameAs(this.ob1);
          ChildEntity singularReference2 = parentEntity2.singularReference;
          ChildEntity childEntity2 = parentEntity2.childEntities.get(0);
          assertThat(singularReference2).isSameAs(childEntity2);

          assertThat(childEntity1).isNotSameAs(childEntity2);
        },
        x -> {});
  }

  @Test
  void findAllReferenceLoopTest() {

    Entity referenceTestDatastoreEntity =
        Entity.newBuilder(this.key1)
            .set("sibling", this.key1)
            .set("lazyChildren", ListValue.of(this.key2))
            .set("lazyChild", this.childKey2)
            .build();

    Entity child = Entity.newBuilder(this.key1).build();
    Entity child2 = Entity.newBuilder(this.childKey2).build();

    when(this.datastore.fetch(this.key1))
        .thenReturn(Collections.singletonList(referenceTestDatastoreEntity));
    when(this.datastore.fetch(this.key2)).thenReturn(Collections.singletonList(child));
    when(this.datastore.fetch(this.childKey2)).thenReturn(Collections.singletonList(child2));

    ReferenceTestEntity referenceTestEntity = new ReferenceTestEntity();
    ReferenceTestEntity childEntity = new ReferenceTestEntity();
    ReferenceTestEntity childEntity2 = new ReferenceTestEntity();

    DatastorePersistentEntity referenceTestPersistentEntity =
        new DatastoreMappingContext().getDatastorePersistentEntity(ReferenceTestEntity.class);

    when(this.datastoreEntityConverter.read(
            eq(ReferenceTestEntity.class), same(referenceTestDatastoreEntity)))
        .thenAnswer(invocationOnMock -> referenceTestEntity);
    when(this.datastoreEntityConverter.getDiscriminationPersistentEntity(
            eq(ReferenceTestEntity.class), same(referenceTestDatastoreEntity)))
        .thenReturn(referenceTestPersistentEntity);

    when(this.datastoreEntityConverter.read(eq(ReferenceTestEntity.class), same(child)))
        .thenAnswer(invocationOnMock -> childEntity);
    when(this.datastoreEntityConverter.getDiscriminationPersistentEntity(
            eq(ReferenceTestEntity.class), same(child)))
        .thenReturn(referenceTestPersistentEntity);
    when(this.datastoreEntityConverter.read(eq(ReferenceTestEntity.class), same(child2)))
        .thenAnswer(invocationOnMock -> childEntity2);
    when(this.datastoreEntityConverter.getDiscriminationPersistentEntity(
            eq(ReferenceTestEntity.class), same(child2)))
        .thenReturn(referenceTestPersistentEntity);

    verifyBeforeAndAfterEvents(
        null,
        new AfterFindByKeyEvent(
            Collections.singletonList(referenceTestEntity), Collections.singleton(this.key1)),
        () -> {
          ReferenceTestEntity readReferenceTestEntity =
              this.datastoreTemplate.findById(this.key1, ReferenceTestEntity.class);

          assertThat(readReferenceTestEntity.sibling).isSameAs(readReferenceTestEntity);
          verify(this.datastore, times(1)).fetch(any());

          assertThat(readReferenceTestEntity.lazyChildren).hasSize(1);
          verify(this.datastore, times(2)).fetch(any());
          verify(this.datastore, times(1)).fetch(this.key1);
          verify(this.datastore, times(1)).fetch(this.key2);

          assertThat(readReferenceTestEntity.lazyChild.toString()).isNotNull();
          verify(this.datastore, times(3)).fetch(any());
          verify(this.datastore, times(1)).fetch(this.childKey2);
        },
        x -> {});
  }

  @Test
  void saveReferenceLoopTest() {
    ReferenceTestEntity referenceTestEntity = new ReferenceTestEntity();
    referenceTestEntity.id = 1L;
    referenceTestEntity.sibling = referenceTestEntity;
    when(this.objectToKeyFactory.getKeyFromObject(eq(referenceTestEntity), any()))
        .thenReturn(this.key1);

    List<Object[]> callsArgs =
        gatherVarArgCallsArgs(
            this.datastore.put(ArgumentMatchers.<FullEntity[]>any()),
            Collections.singletonList(this.e1));

    assertThat(this.datastoreTemplate.save(referenceTestEntity))
        .isInstanceOf(ReferenceTestEntity.class);

    Entity writtenEntity = Entity.newBuilder(this.key1).set("sibling", this.key1).build();

    assertArgs(
        callsArgs,
        new MapBuilder<List, Integer>()
            .put(Collections.singletonList(writtenEntity), 1)
            .buildModifiable());
  }

  @Test
  void saveTest() {
    saveTestCommon(this.ob1, false);
  }

  @Test
  void saveTestCollectionLazy() {
    this.ob1.lazyMultipleReference =
        LazyUtil.wrapSimpleLazyProxy(
            () -> Collections.singletonList(this.childEntity7),
            List.class,
            ListValue.of(KeyValue.of(this.childKey7)));
    saveTestCommon(this.ob1, true);
  }

  @Test
  void saveTestNotInterfaceLazy() {
    ArrayList<ChildEntity> arrayList = new ArrayList();
    arrayList.add(this.childEntity7);
    this.ob1.lazyMultipleReference =
        LazyUtil.wrapSimpleLazyProxy(
            () -> arrayList, ArrayList.class, ListValue.of(KeyValue.of(this.childKey7)));
    saveTestCommon(this.ob1, true);
  }

  void saveTestCommon(TestEntity parent, boolean lazy) {
    Entity writtenEntity =
        Entity.newBuilder(this.key1)
            .set("singularReference", this.childKey4)
            .set(
                "multipleReference",
                Arrays.asList(KeyValue.of(this.childKey5), KeyValue.of(this.childKey6)))
            .set("lazyMultipleReference", Collections.singletonList(KeyValue.of(this.childKey7)))
            .build();

    Entity writtenChildEntity2 = Entity.newBuilder(this.childKey2).build();
    Entity writtenChildEntity3 = Entity.newBuilder(this.childKey3).build();
    Entity writtenChildEntity4 = Entity.newBuilder(this.childKey4).build();
    Entity writtenChildEntity5 = Entity.newBuilder(this.childKey5).build();
    Entity writtenChildEntity6 = Entity.newBuilder(this.childKey6).build();
    Entity writtenChildEntity7 = Entity.newBuilder(this.childKey7).build();

    doAnswer(
            invocation -> {
              Object[] arguments = invocation.getArguments();
              assertThat(arguments).contains(writtenEntity);
              assertThat(arguments).contains(writtenChildEntity2);
              assertThat(arguments).contains(writtenChildEntity3);
              assertThat(arguments).contains(writtenChildEntity4);
              assertThat(arguments).contains(writtenChildEntity5);
              assertThat(arguments).contains(writtenChildEntity6);
              if (lazy) {
                assertThat(arguments).hasSize(6);
              } else {
                assertThat(arguments).contains(writtenChildEntity7);
                assertThat(arguments).hasSize(7);
              }

              return null;
            })
        .when(this.datastore)
        .put(ArgumentMatchers.<FullEntity[]>any());

    assertThat(this.datastoreTemplate.save(parent)).isInstanceOf(TestEntity.class);
    verify(this.datastore, times(1)).put(ArgumentMatchers.<FullEntity[]>any());
    verify(this.datastoreEntityConverter, times(1)).write(same(parent), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity2), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity3), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity4), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity5), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity6), notNull());
    if (lazy) {
      verify(this.datastoreEntityConverter, times(0)).write(same(this.childEntity7), notNull());
    } else {
      verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity7), notNull());
    }
  }

  private List<Object[]> gatherVarArgCallsArgs(Object methodCall, List<Entity> returnVal) {
    List<Object[]> callsArgs = new ArrayList<>();
    when(methodCall)
        .thenAnswer(
            invocationOnMock -> {
              callsArgs.add(invocationOnMock.getArguments());
              return returnVal;
            });
    return callsArgs;
  }

  private void assertArgs(List<Object[]> callsArgs, Map<List, Integer> expected) {
    for (Object[] args : callsArgs) {
      List<Object> key = Arrays.asList(args);
      expected.put(key, expected.computeIfAbsent(key, k -> 0) - 1);
    }
    expected.forEach(
        (key, value) -> assertThat(value).as("Extra calls with argument " + key).isZero());
  }

  @Test
  void saveTestNonKeyId() {

    Key testKey = createFakeKey("key0");
    assertThatThrownBy(() -> this.datastoreTemplate.save(this.ob1, testKey))
            .isInstanceOf(DatastoreDataException.class)
            .hasMessage("Only Key types are allowed for descendants id");

  }

  @Test
  void saveTestNullDescendantsAndReferences() {
    // making sure save works when descendants are null
    assertThat(this.ob2.childEntities).isNull();
    assertThat(this.ob2.singularReference).isNull();
    assertThat(this.ob2.multipleReference).isNull();

    List<Object[]> callsArgs =
        gatherVarArgCallsArgs(
            this.datastore.put(ArgumentMatchers.<FullEntity[]>any()),
            Collections.singletonList(this.e1));

    this.datastoreTemplate.save(this.ob2);

    assertArgs(
        callsArgs,
        new MapBuilder<List, Integer>()
            .put(Collections.singletonList(Entity.newBuilder(this.key2).build()), 1)
            .buildModifiable());
  }

  @Test
  void saveTestKeyNoAncestor() {

    when(this.objectToKeyFactory.getKeyFromObject(eq(this.childEntity1), any()))
        .thenReturn(this.childEntity1.id);

    Key testKey = createFakeKey("key0");
    assertThatThrownBy(() -> this.datastoreTemplate.save(this.childEntity1, testKey))
            .isInstanceOf(DatastoreDataException.class)
            .hasMessage("Descendant object has a key without current ancestor");
  }

  @Test
  void saveTestKeyWithAncestor() {
    Key key0 = createFakeKey("key0");
    Key keyA =
        Key.newBuilder(key0)
            .addAncestor(PathElement.of(key0.getKind(), key0.getName()))
            .setName("keyA")
            .build();
    ChildEntity childEntity = new ChildEntity();
    childEntity.id = keyA;
    when(this.objectToKeyFactory.getKeyFromObject(eq(childEntity), any())).thenReturn(keyA);
    List<Object[]> callsArgs =
        gatherVarArgCallsArgs(
            this.datastore.put(ArgumentMatchers.<FullEntity[]>any()),
            Collections.singletonList(this.e1));

    this.datastoreTemplate.save(childEntity, key0);

    Entity writtenChildEntity = Entity.newBuilder(keyA).build();

    assertArgs(
        callsArgs,
        new MapBuilder<List, Integer>()
            .put(Collections.singletonList(writtenChildEntity), 1)
            .buildModifiable());
  }

  @Test
  void saveAndAllocateIdTest() {
    when(this.objectToKeyFactory.allocateKeyForObject(same(this.ob1), any())).thenReturn(this.key1);
    Entity writtenEntity1 =
        Entity.newBuilder(this.key1)
            .set("singularReference", this.childKey4)
            .set(
                "multipleReference",
                Arrays.asList(KeyValue.of(this.childKey5), KeyValue.of(this.childKey6)))
            .set("lazyMultipleReference", Collections.singletonList(KeyValue.of(this.childKey7)))
            .build();
    Entity writtenChildEntity2 = Entity.newBuilder(this.childKey2).build();
    Entity writtenChildEntity3 = Entity.newBuilder(this.childKey3).build();
    Entity writtenChildEntity4 = Entity.newBuilder(this.childKey4).build();
    Entity writtenChildEntity5 = Entity.newBuilder(this.childKey5).build();
    Entity writtenChildEntity6 = Entity.newBuilder(this.childKey6).build();
    Entity writtenChildEntity7 = Entity.newBuilder(this.childKey7).build();
    doAnswer(
            invocation -> {
              assertThat(invocation.getArguments())
                  .containsExactlyInAnyOrder(
                      writtenChildEntity2,
                      writtenChildEntity3,
                      writtenChildEntity4,
                      writtenChildEntity5,
                      writtenChildEntity6,
                      writtenEntity1,
                      writtenChildEntity7);
              return null;
            })
        .when(this.datastore)
        .put(ArgumentMatchers.<FullEntity[]>any());

    assertThat(this.datastoreTemplate.save(this.ob1)).isInstanceOf(TestEntity.class);

    verify(this.datastore, times(1)).put(ArgumentMatchers.<FullEntity[]>any());

    verify(this.datastoreEntityConverter, times(1)).write(same(this.ob1), notNull());
  }

  @Test
  void saveAllTest() {
    when(this.objectToKeyFactory.allocateKeyForObject(same(this.ob1), any())).thenReturn(this.key1);
    when(this.objectToKeyFactory.getKeyFromObject(same(this.ob2), any())).thenReturn(this.key2);
    Entity writtenEntity1 =
        Entity.newBuilder(this.key1)
            .set("singularReference", this.childKey4)
            .set(
                "multipleReference",
                Arrays.asList(KeyValue.of(this.childKey5), KeyValue.of(this.childKey6)))
            .set("lazyMultipleReference", Collections.singletonList(KeyValue.of(this.childKey7)))
            .build();

    Entity writtenEntity2 = Entity.newBuilder(this.key2).build();
    Entity writtenChildEntity2 = Entity.newBuilder(this.childKey2).build();
    Entity writtenChildEntity3 = Entity.newBuilder(this.childKey3).build();
    Entity writtenChildEntity4 = Entity.newBuilder(this.childKey4).build();
    Entity writtenChildEntity5 = Entity.newBuilder(this.childKey5).build();
    Entity writtenChildEntity6 = Entity.newBuilder(this.childKey6).build();
    Entity writtenChildEntity7 = Entity.newBuilder(this.childKey7).build();
    doAnswer(
            invocation -> {
              assertThat(invocation.getArguments())
                  .containsExactlyInAnyOrder(
                      writtenChildEntity2,
                      writtenChildEntity3,
                      writtenChildEntity4,
                      writtenChildEntity5,
                      writtenChildEntity6,
                      writtenEntity1,
                      writtenEntity2,
                      writtenChildEntity7);
              return null;
            })
        .when(this.datastore)
        .put(ArgumentMatchers.<FullEntity[]>any());

    List<Entity> expected =
        Arrays.asList(
            writtenChildEntity2,
            writtenChildEntity3,
            writtenChildEntity7,
            writtenChildEntity4,
            writtenChildEntity5,
            writtenChildEntity6,
            writtenEntity1,
            writtenEntity2);
    List javaExpected = Arrays.asList(this.ob1, this.ob2);

    verifyBeforeAndAfterEvents(
        new BeforeSaveEvent(javaExpected),
        new AfterSaveEvent(expected, javaExpected),
        () -> this.datastoreTemplate.saveAll(Arrays.asList(this.ob1, this.ob2)),
        x -> {});

    verify(this.datastoreEntityConverter, times(1)).write(same(this.ob1), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.ob2), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity2), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity3), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity4), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity5), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity6), notNull());
    verify(this.datastore, times(1)).put(ArgumentMatchers.<FullEntity[]>any());
  }

  @Test
  void saveAllMaxWriteSizeTest() {
    when(this.objectToKeyFactory.allocateKeyForObject(same(this.ob1), any())).thenReturn(this.key1);
    when(this.objectToKeyFactory.getKeyFromObject(same(this.ob2), any())).thenReturn(this.key2);
    Entity writtenEntity1 =
        Entity.newBuilder(this.key1)
            .set("singularReference", this.childKey4)
            .set(
                "multipleReference",
                Arrays.asList(KeyValue.of(this.childKey5), KeyValue.of(this.childKey6)))
            .set("lazyMultipleReference", Collections.singletonList(KeyValue.of(this.childKey7)))
            .build();
    Entity writtenEntity2 = Entity.newBuilder(this.key2).build();
    Entity writtenChildEntity2 = Entity.newBuilder(this.childKey2).build();
    Entity writtenChildEntity3 = Entity.newBuilder(this.childKey3).build();
    Entity writtenChildEntity4 = Entity.newBuilder(this.childKey4).build();
    Entity writtenChildEntity5 = Entity.newBuilder(this.childKey5).build();
    Entity writtenChildEntity6 = Entity.newBuilder(this.childKey6).build();
    Entity writtenChildEntity7 = Entity.newBuilder(this.childKey7).build();
    Set<Entity> entities = new HashSet<>();
    entities.addAll(
        Arrays.asList(
            writtenChildEntity2,
            writtenChildEntity3,
            writtenChildEntity7,
            writtenChildEntity4,
            writtenChildEntity5,
            writtenChildEntity6,
            writtenEntity1,
            writtenEntity2));
    doAnswer(
            invocation -> {
              assertThat(invocation.getArguments()).hasSize(1);
              assertThat(entities).contains((Entity) invocation.getArguments()[0]);
              entities.remove(invocation.getArguments()[0]);
              return null;
            })
        .when(this.datastore)
        .put(ArgumentMatchers.<FullEntity[]>any());

    List<Entity> expected =
        Arrays.asList(
            writtenChildEntity2,
            writtenChildEntity3,
            writtenChildEntity7,
            writtenChildEntity4,
            writtenChildEntity5,
            writtenChildEntity6,
            writtenEntity1,
            writtenEntity2);
    List javaExpected = Arrays.asList(this.ob1, this.ob2);

    this.datastoreTemplate.setMaxWriteSize(1);
    verifyBeforeAndAfterEvents(
        new BeforeSaveEvent(javaExpected),
        new AfterSaveEvent(expected, javaExpected),
        () -> this.datastoreTemplate.saveAll(Arrays.asList(this.ob1, this.ob2)),
        x -> {});

    assertThat(entities).isEmpty();

    verify(this.datastoreEntityConverter, times(1)).write(same(this.ob1), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.ob2), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity2), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity3), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity4), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity5), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity6), notNull());
    verify(this.datastoreEntityConverter, times(1)).write(same(this.childEntity7), notNull());

    verify(this.datastore, times(8)).put(ArgumentMatchers.<FullEntity[]>any());
  }

  @Test
  void findAllTest() {
    verifyBeforeAndAfterEvents(
        null,
        new AfterQueryEvent(Arrays.asList(this.ob1, this.ob2), this.findAllTestEntityQuery),
        () ->
            assertThat(this.datastoreTemplate.findAll(TestEntity.class))
                .contains(this.ob1, this.ob2),
        x -> {});
  }

  @Test
  void queryTest() {
    verifyBeforeAndAfterEvents(
        null,
        new AfterQueryEvent(Arrays.asList(this.ob1, this.ob2), this.testEntityQuery),
        () ->
            assertThat(
                    this.datastoreTemplate.query(
                        (Query<Entity>) this.testEntityQuery, TestEntity.class))
                .contains(this.ob1, this.ob2),
        x -> {});
  }

  @Test
  @SuppressWarnings("ReturnValueIgnored")
  void queryKeysTest() {
    KeyQuery keyQuery = GqlQuery.newKeyQueryBuilder().build();
    this.datastoreTemplate.queryKeys(keyQuery).iterator();
    verify(this.datastore, times(1)).run(keyQuery);
  }

  @Test
  void nextPageTest() {
    assertThat(nextPageTest(true)).isTrue();
    assertThat(nextPageTest(false)).isFalse();
  }

  private boolean nextPageTest(boolean hasNextPage) {
    QueryResults<Key> queryResults = mock(QueryResults.class);
    when(queryResults.getResultClass()).thenReturn((Class) Key.class);
    doAnswer(
            invocation -> {
              Arrays.asList(this.key1, this.key2)
                  .iterator()
                  .forEachRemaining(invocation.getArgument(0));
              return null;
            })
        .when(queryResults)
        .forEachRemaining(any());
    Cursor cursor = Cursor.copyFrom("abc".getBytes());
    when(queryResults.getCursorAfter()).thenReturn(cursor);

    KeyQuery query = Query.newKeyQueryBuilder().setKind("custom_test_kind").setLimit(1).build();
    when(this.datastore.run(query)).thenReturn(queryResults);

    QueryResults<Key> nextPageQueryResults = mock(QueryResults.class);
    when(nextPageQueryResults.hasNext()).thenReturn(hasNextPage);

    KeyQuery nextPageQuery =
        query.toBuilder().setStartCursor(cursor).setOffset(0).setLimit(1).build();

    when(this.datastore.run(nextPageQuery)).thenReturn(nextPageQueryResults);

    Slice<Key> resultsSlice =
        this.datastoreTemplate.queryKeysSlice(query, TestEntity.class, PageRequest.of(0, 1));
    return resultsSlice.hasNext();
  }

  @Test
  void countTest() {
    QueryResults<Key> queryResults = mock(QueryResults.class);
    when(queryResults.getResultClass()).thenReturn((Class) Key.class);
    doAnswer(
            invocation -> {
              Arrays.asList(this.key1, this.key2)
                  .iterator()
                  .forEachRemaining(invocation.getArgument(0));
              return null;
            })
        .when(queryResults)
        .forEachRemaining(any());
    when(this.datastore.run(Query.newKeyQueryBuilder().setKind("custom_test_kind").build()))
        .thenReturn(queryResults);
    assertThat(this.datastoreTemplate.count(TestEntity.class)).isEqualTo(2);
  }

  @Test
  void existsByIdTest() {
    assertThat(this.datastoreTemplate.existsById(this.key1, TestEntity.class)).isTrue();
    assertThat(this.datastoreTemplate.existsById(this.badKey, TestEntity.class)).isFalse();
  }

  @Test
  void deleteByIdTest() {
    when(this.objectToKeyFactory.getKeyFromId(same(this.key1), any())).thenReturn(this.key1);

    verifyBeforeAndAfterEvents(
        new BeforeDeleteEvent(
            new Key[] {this.key1}, TestEntity.class, Collections.singletonList(this.key1), null),
        new AfterDeleteEvent(
            new Key[] {this.key1}, TestEntity.class, Collections.singletonList(this.key1), null),
        () -> this.datastoreTemplate.deleteById(this.key1, TestEntity.class),
        x -> x.verify(this.datastore, times(1)).delete(same(this.key1)));
  }

  @Test
  void deleteAllByIdTest() {
    when(this.objectToKeyFactory.getKeyFromId(same(this.key1), any())).thenReturn(this.key1);
    when(this.objectToKeyFactory.getKeyFromId(same(this.key2), any())).thenReturn(this.key2);

    verifyBeforeAndAfterEvents(
        new BeforeDeleteEvent(
            new Key[] {this.key2, this.key1},
            TestEntity.class,
            Arrays.asList(this.key1, this.key2),
            null),
        new AfterDeleteEvent(
            new Key[] {this.key2, this.key1},
            TestEntity.class,
            Arrays.asList(this.key1, this.key2),
            null),
        () ->
            this.datastoreTemplate.deleteAllById(
                Arrays.asList(this.key1, this.key2), TestEntity.class),
        x -> x.verify(this.datastore, times(1)).delete(same(this.key2), same(this.key1)));
  }

  @Test
  void deleteObjectTest() {
    verifyBeforeAndAfterEvents(
        new BeforeDeleteEvent(
            new Key[] {this.key1}, TestEntity.class, null, Collections.singletonList(this.ob1)),
        new AfterDeleteEvent(
            new Key[] {this.key1}, TestEntity.class, null, Collections.singletonList(this.ob1)),
        () -> this.datastoreTemplate.delete(this.ob1),
        x -> x.verify(this.datastore, times(1)).delete(same(this.key1)));
  }

  @Test
  void deleteMultipleObjectsTest() {
    verifyBeforeAndAfterEvents(
        new BeforeDeleteEvent(
            new Key[] {this.key1, this.key2}, null, null, Arrays.asList(this.ob1, this.ob2)),
        new AfterDeleteEvent(
            new Key[] {this.key1, this.key2}, null, null, Arrays.asList(this.ob1, this.ob2)),
        () -> this.datastoreTemplate.deleteAll(Arrays.asList(this.ob1, this.ob2)),
        x -> x.verify(this.datastore, times(1)).delete(this.key1, this.key2));
  }

  @Test
  void deleteAllTest() {
    QueryResults<Key> queryResults = mock(QueryResults.class);
    when(queryResults.getResultClass()).thenReturn((Class) Key.class);
    doAnswer(
            invocation -> {
              Arrays.asList(this.key1, this.key2)
                  .iterator()
                  .forEachRemaining(invocation.getArgument(0));
              return null;
            })
        .when(queryResults)
        .forEachRemaining(any());
    when(this.datastore.run(Query.newKeyQueryBuilder().setKind("custom_test_kind").build()))
        .thenReturn(queryResults);

    verifyBeforeAndAfterEvents(
        new BeforeDeleteEvent(new Key[] {this.key1, this.key2}, TestEntity.class, null, null),
        new AfterDeleteEvent(new Key[] {this.key1, this.key2}, TestEntity.class, null, null),
        () -> assertThat(this.datastoreTemplate.deleteAll(TestEntity.class)).isEqualTo(2),
        x -> x.verify(this.datastore, times(1)).delete(same(this.key1), same(this.key2)));
  }

  private void verifyBeforeAndAfterEvents(
      ApplicationEvent expectedBefore,
      ApplicationEvent expectedAfter,
      Runnable operation,
      Consumer<InOrder> verifyOperation) {
    ApplicationEventPublisher mockPublisher = mock(ApplicationEventPublisher.class);
    ApplicationEventPublisher mockBeforePublisher = mock(ApplicationEventPublisher.class);
    ApplicationEventPublisher mockAfterPublisher = mock(ApplicationEventPublisher.class);

    InOrder inOrder = Mockito.inOrder(mockBeforePublisher, this.datastore, mockAfterPublisher);

    doAnswer(
            invocationOnMock -> {
              ApplicationEvent event = invocationOnMock.getArgument(0);
              if (expectedBefore != null && event.getClass().equals(expectedBefore.getClass())) {
                mockBeforePublisher.publishEvent(event);
              } else if (expectedAfter != null
                  && event.getClass().equals(expectedAfter.getClass())) {
                mockAfterPublisher.publishEvent(event);
              }
              return null;
            })
        .when(mockPublisher)
        .publishEvent(any());

    this.datastoreTemplate.setApplicationEventPublisher(mockPublisher);

    operation.run();
    if (expectedBefore != null) {
      inOrder.verify(mockBeforePublisher, times(1)).publishEvent(expectedBefore);
    }
    verifyOperation.accept(inOrder);
    if (expectedAfter != null) {
      inOrder.verify(mockAfterPublisher, times(1)).publishEvent(expectedAfter);
    }
  }

  @Test
  void findAllTestLimitOffset() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("custom_test_kind");

    this.datastoreTemplate.findAll(
        TestEntity.class, new DatastoreQueryOptions.Builder().setLimit(1).setOffset(5).build());
    verify(this.datastore, times(1)).run(builder.setLimit(1).setOffset(5).build());

    this.datastoreTemplate.findAll(TestEntity.class, new DatastoreQueryOptions.Builder().build());
    verify(this.datastore, times(1)).run(builder.build());
  }

  @Test
  void findAllDiscrimination() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("test_kind");

    this.datastoreTemplate.findAll(SimpleDiscriminationTestEntity.class);
    verify(this.datastore, times(1))
        .run(builder.setFilter(PropertyFilter.eq("discrimination_field", "A")).build());
  }

  @Test
  void combineFiltersDiscrimination() {
    PropertyFilter propertyFilter = PropertyFilter.eq("field", "some value");
    EntityQuery.Builder builder =
        Query.newEntityQueryBuilder().setKind("test_kind").setFilter(propertyFilter);
    DatastoreTemplate.applyQueryOptions(
        builder,
        new DatastoreQueryOptions.Builder().setLimit(1).setOffset(2).build(),
        new DatastoreMappingContext().getPersistentEntity(SimpleDiscriminationTestEntity.class));

    assertThat(builder.build().getFilter())
        .isEqualTo(
            StructuredQuery.CompositeFilter.and(
                propertyFilter, PropertyFilter.eq("discrimination_field", "A")));

    assertThat(builder.build().getLimit()).isEqualTo(1);
    assertThat(builder.build().getOffset()).isEqualTo(2);
  }

  @Test
  void findAllTestSort() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("custom_test_kind");

    this.datastoreTemplate.findAll(
        TestEntity.class,
        new DatastoreQueryOptions.Builder().setSort(Sort.by("sortProperty")).build());
    verify(this.datastore, times(1))
        .run(
            builder
                .setOrderBy(
                    new StructuredQuery.OrderBy(
                        "prop", StructuredQuery.OrderBy.Direction.ASCENDING))
                .build());

    this.datastoreTemplate.findAll(
        TestEntity.class,
        new DatastoreQueryOptions.Builder()
            .setSort(Sort.by(Sort.Direction.DESC, "sortProperty"))
            .build());
    verify(this.datastore, times(1))
        .run(
            builder
                .setOrderBy(
                    new StructuredQuery.OrderBy(
                        "prop", StructuredQuery.OrderBy.Direction.DESCENDING))
                .build());
  }

  @Test
  void findAllTestSortLimitOffset() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("custom_test_kind");

    this.datastoreTemplate.findAll(
        TestEntity.class,
        new DatastoreQueryOptions.Builder()
            .setLimit(2)
            .setOffset(3)
            .setSort(Sort.by("sortProperty"))
            .build());
    verify(this.datastore, times(1))
        .run(
            builder
                .setLimit(2)
                .setOffset(3)
                .setOrderBy(
                    new StructuredQuery.OrderBy(
                        "prop", StructuredQuery.OrderBy.Direction.ASCENDING))
                .build());
  }

  @Test
  void queryByExampleSimpleEntityTest() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("test_kind");
    StructuredQuery.CompositeFilter filter =
        StructuredQuery.CompositeFilter.and(
            PropertyFilter.eq("color", "simple_test_color"), PropertyFilter.eq("int_field", 1));
    EntityQuery query = builder.setFilter(filter).build();
    verifyBeforeAndAfterEvents(
        null,
        new AfterQueryEvent(Collections.emptyList(), query),
        () ->
            this.datastoreTemplate.queryByExample(
                Example.of(this.simpleTestEntity, ExampleMatcher.matching().withIgnorePaths("id")),
                null),
        x -> x.verify(this.datastore, times(1)).run(query));
  }

  @Test
  void queryByExampleIgnoreFieldTest() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("test_kind");
    this.datastoreTemplate.queryByExample(
        Example.of(
            this.simpleTestEntity, ExampleMatcher.matching().withIgnorePaths("id", "intField")),
        null);

    StructuredQuery.CompositeFilter filter =
        StructuredQuery.CompositeFilter.and(PropertyFilter.eq("color", "simple_test_color"));
    verify(this.datastore, times(1)).run(builder.setFilter(filter).build());
  }

  @Test
  void queryByExampleDeepPathTest() {

    Example testExample = Example.of(new SimpleTestEntity(), ExampleMatcher.matching().withIgnorePaths("intField.a"));
    assertThatThrownBy(() -> this.datastoreTemplate.queryByExample(testExample, null))
            .hasMessage("Ignored paths deeper than 1 are not supported")
            .isInstanceOf(DatastoreDataException.class);
  }

  @Test
  void queryByExampleIncludeNullValuesTest() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("test_kind");
    this.datastoreTemplate.queryByExample(
        Example.of(
            this.simpleTestEntityNullVallues,
            ExampleMatcher.matching().withIgnorePaths("id").withIncludeNullValues()),
        null);

    StructuredQuery.CompositeFilter filter =
        StructuredQuery.CompositeFilter.and(
            PropertyFilter.eq("color", NullValue.of()),
            PropertyFilter.eq("int_field", NullValue.of()));
    verify(this.datastore, times(1)).run(builder.setFilter(filter).build());
  }

  @Test
  void queryByExampleNoNullValuesTest() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("test_kind");
    this.datastoreTemplate.queryByExample(
        Example.of(
            this.simpleTestEntityNullVallues, ExampleMatcher.matching().withIgnorePaths("id")),
        null);

    verify(this.datastore, times(1)).run(builder.build());
  }

  @Test
  void queryByExampleExactMatchTest() {

    Example testExample = Example.of(new SimpleTestEntity(), ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.REGEX));
    assertThatThrownBy(() -> this.datastoreTemplate.queryByExample(testExample, null))
            .hasMessage("Unsupported StringMatcher. Only EXACT and DEFAULT are supported")
            .isInstanceOf(DatastoreDataException.class);
  }

  @Test
  void queryByExampleIgnoreCaseTest() {

    Example testExample = Example.of(new SimpleTestEntity(), ExampleMatcher.matching().withIgnoreCase());
    assertThatThrownBy(() -> this.datastoreTemplate.queryByExample(testExample, null))
            .hasMessage("Ignore case matching is not supported")
            .isInstanceOf(DatastoreDataException.class);
  }

  @Test
  void queryByExampleAllMatchTest() {

    Example testExample = Example.of(new SimpleTestEntity(), ExampleMatcher.matchingAny());
    assertThatThrownBy(() -> this.datastoreTemplate.queryByExample(testExample, null))
            .hasMessage("Unsupported MatchMode. Only MatchMode.ALL is supported")
            .isInstanceOf(DatastoreDataException.class);
  }

  @Test
  void queryByExamplePropertyMatchersTest() {

    Example testExample = Example.of(
            new SimpleTestEntity(),
            ExampleMatcher.matching()
                    .withMatcher(
                            "id",
                            ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.REGEX)));
    assertThatThrownBy(() ->   this.datastoreTemplate.queryByExample(testExample, null))
            .hasMessage("Property matchers are not supported")
            .isInstanceOf(DatastoreDataException.class);
  }

  @Test
  void queryByExampleCaseSensitiveTest() {

    Example testExample =  Example.of(
            new SimpleTestEntity(),
            ExampleMatcher.matching()
                    .withMatcher("id", ExampleMatcher.GenericPropertyMatcher::caseSensitive));
    assertThatThrownBy(() -> this.datastoreTemplate.queryByExample(testExample, null))
            .hasMessage("Property matchers are not supported")
            .isInstanceOf(DatastoreDataException.class);
  }

  @Test
  void queryByExampleNullTest() {

    assertThatThrownBy(() -> this.datastoreTemplate.queryByExample(null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("A non-null example is expected");
  }

  @Test
  void queryByExampleOptions() {
    EntityQuery.Builder builder = Query.newEntityQueryBuilder().setKind("test_kind");
    this.datastoreTemplate.queryByExample(
        Example.of(this.simpleTestEntity, ExampleMatcher.matching().withIgnorePaths("id")),
        new DatastoreQueryOptions.Builder()
            .setLimit(10)
            .setOffset(1)
            .setSort(Sort.by("intField"))
            .build());

    StructuredQuery.CompositeFilter filter =
        StructuredQuery.CompositeFilter.and(
            PropertyFilter.eq("color", "simple_test_color"), PropertyFilter.eq("int_field", 1));
    verify(this.datastore, times(1))
        .run(
            builder
                .setFilter(filter)
                .addOrderBy(StructuredQuery.OrderBy.asc("int_field"))
                .setLimit(10)
                .setOffset(1)
                .build());
  }

  @Test
  void writeMapTest() {
    Map<String, Long> map = new HashMap<>();
    map.put("field1", 1L);
    Key keyForMap = createFakeKey("map1");

    when(this.readWriteConversions.convertOnWriteSingle(1L)).thenReturn(LongValue.of(1L));

    this.datastoreTemplate.writeMap(keyForMap, map);

    Entity datastoreEntity = Entity.newBuilder(keyForMap).set("field1", 1L).build();
    verify(this.datastore, times(1)).put(datastoreEntity);
  }

  @Test
  void findByIdAsMapTest() {
    Key keyForMap = createFakeKey("map1");

    Entity datastoreEntity = Entity.newBuilder(keyForMap).set("field1", 1L).build();
    when(this.datastore.get(keyForMap)).thenReturn(datastoreEntity);

    this.datastoreTemplate.findByIdAsMap(keyForMap, Long.class);
    verify(this.datastoreEntityConverter, times(1))
        .readAsMap(String.class, TypeInformation.of(Long.class), datastoreEntity);
  }

  @Test
  void createKeyTest() {
    this.datastoreTemplate.createKey("kind1", 1L);
    verify(this.objectToKeyFactory, times(1)).getKeyFromId(1L, "kind1");
  }

  @com.google.cloud.spring.data.datastore.core.mapping.Entity(name = "custom_test_kind")
  private static class TestEntity {
    @Id String id;

    @Field(name = "prop")
    String sortProperty;

    @Descendants LinkedList<ChildEntity> childEntities;

    @Reference ChildEntity singularReference;

    @Reference LinkedList<ChildEntity> multipleReference;

    @LazyReference List<ChildEntity> lazyMultipleReference;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestEntity that = (TestEntity) o;
      return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.id);
    }
  }

  @com.google.cloud.spring.data.datastore.core.mapping.Entity(name = "child_entity")
  private static class ChildEntity {
    @Id Key id;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ChildEntity that = (ChildEntity) o;
      return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.id);
    }
  }

  @com.google.cloud.spring.data.datastore.core.mapping.Entity(name = "test_kind")
  private static class SimpleTestEntity {
    @Id String id;

    String color;

    @Field(name = "int_field")
    int intField;
  }

  @com.google.cloud.spring.data.datastore.core.mapping.Entity(name = "test_kind")
  @DiscriminatorField(field = "discrimination_field")
  @DiscriminatorValue("A")
  private static class SimpleDiscriminationTestEntity {
    @Id String id;

    @Field(name = "int_field")
    int intField;
  }

  class ReferenceTestEntity {
    @Id Long id;

    @Reference ReferenceTestEntity sibling;

    @LazyReference List<ReferenceTestEntity> lazyChildren;

    @LazyReference ReferenceTestEntity lazyChild;

    @Override
    public String toString() {
      return "ReferenceTestEntity{"
          + "id="
          + this.id
          + ", sibling="
          + this.sibling
          + ", lazyChildren="
          + this.lazyChildren
          + ", lazyChild="
          + this.lazyChild
          + '}';
    }
  }
}
