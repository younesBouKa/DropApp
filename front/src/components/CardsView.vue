<template>
    <div style="height: 100%">
        <el-row v-if="dataRows.length===0" class="empty-folder-capture">
            <el-col>
                <span>Empty folder</span>
            </el-col>
        </el-row>
        <el-row v-else
                v-for="row in dataRows"
                type="flex"
                :justify="row.length < nbrOfColumnsInRow ? 'start':'space-between'"
        >
                <el-col :style="colStyle" v-for="node in row">
                    <div class="node-col">
                        <NodeCard
                                :node="node"
                                @click.native="selectNode(node)"
                                @dblclick.native="setCurrentNode(node)"
                                :class="selectedNodes.findIndex(n=>n.id===node.id)!==-1 ? 'selected_node':''"
                        >
                        </NodeCard>
                    </div>
                </el-col>
        </el-row>
    </div>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'
    import NodeCard from '@/components/NodeCard.vue'

    const _ = require('lodash');
    export default {
        name: "CardsView",
        components: {
            NodeCard,
        },
        props: {
            data: {
                type: Array,
                default : ()=>[]
            }
        },
        data() {
            return {
                defaultColWidth : 200,
                containerWidth : 0,
                defaultNbrColsInRow: 4,
                selectedNodes : [],
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {
            currentNodeData(){
                return this.$store.getters.getCurrentNodeData;
            },
            dataRows() {
                return _.chunk(this.data, this.nbrOfColumnsInRow);
            },
            nbrOfColumnsInRow(){
                return !this.containerWidth || this.containerWidth===0
                    ? this.defaultNbrColsInRow
                    : parseInt((this.containerWidth/this.defaultColWidth).toFixed(0),10);
            },
            colStyle(){
                return {
                   width: this.nbrOfColumnsInRow>0 ? (100/this.nbrOfColumnsInRow)+"%" : this.defaultColWidth+"px"
                }
            }
        },
        watch: {
            dataRows(val){
                this.selectedNodes = [];
            }
        },
        mounted() {
            let self = this;
            this.containerWidth = this.$el.getBoundingClientRect().width;
            window.addEventListener('resize', ()=>{
                try {
                    let containerWidth = self.$el.getBoundingClientRect().width;
                    self.containerWidth = containerWidth > self.defaultColWidth ? containerWidth : self.containerWidth;
                }catch (error) {}
            });
        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            selectNode(node){
                console.log("selected node: ",node);
                let index = this.selectedNodes.findIndex(el=>el.id===node.id);
                if(index!==-1)
                    this.selectedNodes.splice(index,1);
                else
                    this.selectedNodes.push(node);
                this.$emit("selectionChanged",this.selectedNodes);
            },
            setCurrentNode(node){
                this.$store.commit("storeCurrentNodeData", node);
            }
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .empty-folder-capture{
        height: 100%;
        display: grid;
        justify-content: center;
        vertical-align: middle;
    }

    .selected_node >>> .el-card{
        background-color: #e3effd;
    }
</style>
