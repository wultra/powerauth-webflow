/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webauth.repository;

import io.getlime.security.powerauth.app.webauth.repository.model.Session;
import org.springframework.data.repository.CrudRepository;

/**
 * Basic CRUD repository for sessions. For now we use an in-memory repository, can be easily converted to a database
 * repository in case we will need to persist sessions.
 * @author Roman Strobl
 */
public interface SessionRepository extends CrudRepository<Session, Long> {

}
