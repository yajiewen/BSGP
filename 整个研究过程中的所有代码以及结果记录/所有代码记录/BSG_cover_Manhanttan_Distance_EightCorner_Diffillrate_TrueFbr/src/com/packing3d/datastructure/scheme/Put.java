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

    public Put(Block block,Space space){
        this.block = block;
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
