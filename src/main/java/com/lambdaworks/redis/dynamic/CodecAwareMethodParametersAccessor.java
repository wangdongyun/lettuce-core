/*
 * Copyright 2011-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lambdaworks.redis.dynamic;

import java.util.Iterator;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.dynamic.parameter.MethodParametersAccessor;
import com.lambdaworks.redis.dynamic.support.ClassTypeInformation;
import com.lambdaworks.redis.dynamic.support.TypeInformation;
import com.lambdaworks.redis.internal.LettuceAssert;

/**
 * @author Mark Paluch
 */
public class CodecAwareMethodParametersAccessor implements MethodParametersAccessor {

    private final MethodParametersAccessor delegate;
    private final TypeInformation<?> keyType;
    private final TypeInformation<?> valueType;

    public CodecAwareMethodParametersAccessor(MethodParametersAccessor delegate, RedisCodec<?, ?> redisCodec) {

        LettuceAssert.notNull(delegate, "MethodParametersAccessor must not be null");
        LettuceAssert.notNull(redisCodec, "RedisCodec must not be null");

        this.delegate = delegate;

        ClassTypeInformation<? extends RedisCodec> typeInformation = ClassTypeInformation.from(redisCodec.getClass());

        this.keyType = typeInformation.getTypeArgument(RedisCodec.class, 0);
        this.valueType = typeInformation.getTypeArgument(RedisCodec.class, 1);

    }

    @Override
    public int getParameterCount() {
        return delegate.getParameterCount();
    }

    @Override
    public Object getBindableValue(int index) {
        return delegate.getBindableValue(index);
    }

    @Override
    public boolean isKey(int index) {

        if (delegate.isValue(index)) {
            return false;
        }

        if (delegate.isKey(index)) {
            return true;
        }

        Object bindableValue = getBindableValue(index);

        if (bindableValue != null && keyType.getType().isAssignableFrom(bindableValue.getClass())) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isValue(int index) {

        if (delegate.isKey(index)) {
            return false;
        }

        if (delegate.isValue(index)) {
            return true;
        }

        Object bindableValue = getBindableValue(index);

        if (bindableValue != null && valueType.getType().isAssignableFrom(bindableValue.getClass())) {
            return true;
        }

        return false;
    }

    @Override
    public Iterator<Object> iterator() {
        return delegate.iterator();
    }

    @Override
    public int resolveParameterIndex(String name) {
        return delegate.resolveParameterIndex(name);
    }

    @Override
    public boolean isBindableNullValue(int index) {
        return delegate.isBindableNullValue(index);
    }
}