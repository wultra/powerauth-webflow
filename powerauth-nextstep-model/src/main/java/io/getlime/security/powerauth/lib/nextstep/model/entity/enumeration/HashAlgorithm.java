/*
 * Copyright 2017 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration;

/**
 * Enumeration representing hashing algorithms.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum HashAlgorithm {

    /**
     * Algorithm argon2d.
     */
    ARGON_2D("argon2d", 0),

    /**
     * Algorithm argon2i.
     */
    ARGON_2I("argon2i", 1),

    /**
     * Algorithm argon2id.
     */
    ARGON_2ID("argon2id", 2);

    private final String name;
    private final int id;

    /**
     * Hash algorithm constructor.
     * @param name Algorithm name for Modular Crypt Format.
     * @param id Algorithm ID in Bouncy Castle library.
     */
    HashAlgorithm(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Get algorithm name for Modular Crypt Format.
     * @return Algorithm name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get algorithm ID in Bouncy Castle library.
     * @return Algorithm ID.
     */
    public int getId() {
        return id;
    }

}
