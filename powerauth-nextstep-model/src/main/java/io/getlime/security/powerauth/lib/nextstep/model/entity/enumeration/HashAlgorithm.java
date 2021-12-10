/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
