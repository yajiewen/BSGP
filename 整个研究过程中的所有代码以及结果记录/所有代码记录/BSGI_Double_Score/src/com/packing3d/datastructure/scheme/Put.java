package com.packing3d.datastructure.scheme;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.space.Space;

/**
 * @Author yajiewen
 * @Date 2022-03-12 17-18-01
 * @Description
*/ 
public class Put {
    public Block block;
    public Space space;

    /**
     * @Author yajiewen
     * @Date 2022-11-05 10-36-18
     * @Description 因为一个块会被使用多次，为了避免相同对象导致的一系列问题，把块克隆后再放入put
    */
    public Put(Block block,Space space){
        this.block = block.cloneOne();
        this.space = space;
    }

    @Override
    public String toString() {
        return "Put{" +
                "block=" + block +
                ", space=" + space +
                '}';
    }
}
