package com.kmose.newsapiclient.data.db

import androidx.room.TypeConverter
import com.kmose.newsapiclient.data.model.Source

/*
Room에 Article이 저장 될 때
Article class의 source가 Source클래스 타입이므로
비효율적으로 Source클래스의 Table을 만들지 않고
Converter 클래스를 이용해서
Source의 인스턴스를 받아서 name만 저장함
 */
class Converters {
    @TypeConverter
    fun fromSource(source: Source):String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}