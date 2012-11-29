/*
 *  Copyright 2012 Hippo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.repository.query.lucene;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.jackrabbit.core.query.lucene.MultiIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationFilter extends Filter {

    static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

    private static class MultiDocIdSet extends DocIdSet {

        private final int[] maxDocs;
        private final DocIdSet[] docIdSets;

        public MultiDocIdSet(final DocIdSet[] docIdSets, final int[] maxDocs) {
            this.docIdSets = docIdSets;
            this.maxDocs = maxDocs;
        }

        @Override
        public DocIdSetIterator iterator() {
            return new DocIdSetIterator() {

                int docOffset = 0;
                int docIdSetIndex = -1;
                DocIdSetIterator currentDocIdSetIterator;

                @Override
                public int doc() {
                    return docOffset + currentDocIdSetIterator.doc();
                }

                @Override
                public boolean next() throws IOException {
                    while (currentDocIdSetIterator == null || !currentDocIdSetIterator.next()) {
                        if (!nextIterator()) {
                            return false;
                        }
                        if (!currentDocIdSetIterator.next()) {
                            currentDocIdSetIterator = null;
                        } else {
                            return true;
                        }
                    }
                    return true;
                }

                private boolean nextIterator() {
                    currentDocIdSetIterator = null;
                    if (docIdSets.length == (docIdSetIndex + 1)) {
                        return false;
                    }
                    if (docIdSetIndex >= 0) {
                        docOffset += maxDocs[docIdSetIndex];
                    }
                    docIdSetIndex++;
                    currentDocIdSetIterator = docIdSets[docIdSetIndex].iterator();
                    return true;
                }

                @Override
                public boolean skipTo(final int target) throws IOException {
                    do {
                        if (!next()) {
                            return false;
                        }
                    } while (target > doc());
                    return true;
                }
            };
        }
    }

    private class IndexReaderFilter {
        private final int maxDoc;
        private final DocIdSet bits;

        IndexReaderFilter(IndexReader reader) throws IOException {
            maxDoc = reader.maxDoc();

            long start = System.currentTimeMillis();

            Filter filter = new QueryWrapperFilter(query);
            this.bits = filter.getDocIdSet(reader);

            long docIdSetCreationTime = System.currentTimeMillis() - start;
            log.info("Creating authorization doc id set took {} ms.", String.valueOf(docIdSetCreationTime));
        }

        private boolean isValid(IndexReader reader) {
            return reader.maxDoc() == maxDoc;
        }
    }

    private final Map<IndexReader, IndexReaderFilter> cache = Collections.synchronizedMap(
            new WeakHashMap<IndexReader, IndexReaderFilter>());
    private final Query query;


    public AuthorizationFilter(final Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
        if (reader instanceof MultiIndexReader) {
            MultiIndexReader multiIndexReader = (MultiIndexReader) reader;

            IndexReader[] indexReaders = multiIndexReader.getIndexReaders();
            DocIdSet[] docIdSets = new DocIdSet[indexReaders.length];
            int[] maxDocs = new int[indexReaders.length];
            for (int i = 0; i < indexReaders.length; i++) {
                IndexReader subReader = indexReaders[i];
                docIdSets[i] = getIndexReaderDocIdSet(subReader);
                maxDocs[i] = subReader.maxDoc();
            }

            return new MultiDocIdSet(docIdSets, maxDocs);
        }
        return getIndexReaderDocIdSet(reader);
    }

    private DocIdSet getIndexReaderDocIdSet(final IndexReader reader) throws IOException {
        IndexReaderFilter filter = cache.get(reader);
        if (filter == null) {
            filter = createFilter(reader);
        }
        return filter.bits;
    }

    private synchronized IndexReaderFilter createFilter(IndexReader reader) throws IOException {
        IndexReaderFilter filter;
        if (cache.containsKey(reader)) {
            filter = cache.get(reader);
            if (filter.isValid(reader)) {
                return filter;
            }
        }
        filter = new IndexReaderFilter(reader);
        cache.put(reader, filter);
        return filter;
    }
}
