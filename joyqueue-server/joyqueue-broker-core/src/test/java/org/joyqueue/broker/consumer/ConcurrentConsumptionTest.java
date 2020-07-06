/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.consumer;

import org.joyqueue.broker.consumer.position.model.Position;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chengzhiliang on 2018/11/1.
 */
public class ConcurrentConsumptionTest {

    @Test
    public void sortByAckStartIndexTest() {
        List<Position> list = new ArrayList<>();
        Position position = new Position(1, 1, 1, 1);
        Position position1 = new Position(2, 2, 2, 2);
        Position position2 = new Position(3, 3, 3, 3);
        Position position3 = new Position(4, 4, 4, 4);
        list.add(position3);
        list.add(position);
        list.add(position2);
        list.add(position1);
        System.out.println(list);
        List<Position> positions = sortByAckStartIndex(list);
        System.out.println(positions);
    }

    private List<Position> sortByAckStartIndex(List<Position> list) {
        return list.stream().sorted((thisPosition, thatPosition) -> (int) (thisPosition.getAckStartIndex() - thatPosition.getAckStartIndex())).collect(Collectors.toList());
    }

    @Test
    public void mergeSequenceSegmentTest() {
        List<Position> list = new ArrayList<>();
        Position position = new Position(1, 1, 1, 1);
//        PositionManager.Position position1 = new PositionManager.Position(2, 2, 2, 2);
        Position position2 = new Position(3, 3, 3, 3);
        Position position3 = new Position(4, 4, 4, 4);
        Position position4 = new Position(5, 5, 5, 5);
        Position position5 = new Position(6, 6, 6, 6);
        list.add(position3);
        list.add(position);
        list.add(position2);
        list.add(position5);
        list.add(position4);
        List<Position> positions = sortByAckStartIndex(list);

        List<Position> positions1 = mergeSequenceSegment(positions);
        System.out.println(positions1);
    }


    private List<Position> mergeSequenceSegment(List<Position> sortPositionList) {
        if (sortPositionList.size() <= 1) {
            return sortPositionList;
        }
        List<Position> mergeList = new ArrayList<>();
        // 初始化两个待比较的值
        Position last = null;
        Position next = null;
        int i = 0;
        int size = sortPositionList.size();
        while (i < size) {
            if (i == 0) {
                last = sortPositionList.get(i);
                next = sortPositionList.get(i += 1);
            }
            Position newPosition = tryMergeTwoSegment(last, next);
            if (newPosition == null) {
                mergeList.add(last);
                last = next;
            } else {
                last = newPosition;
            }
            if (i + 1 < size) {
                next = sortPositionList.get(i + 1);
            } else {
                mergeList.add(last);
            }
            i++;
        }

        return mergeList;
    }

    private Position tryMergeTwoSegment(Position last, Position next) {
        // 包含关系
        if (last.getPullCurIndex() >= next.getPullCurIndex()) {
            return last;
        }
        // 连续关系(两个连续的序号相差1)
        if (last.getAckCurIndex() + 1 == next.getAckStartIndex()) {
            // 这里只关心应答序号，消费序号都用-1标示，稍后再合并消费位置的会填充。
            return new Position(last.getAckStartIndex(), next.getAckCurIndex(), -1, -1);
        }
        // 否则返回空
        return null;
    }
}