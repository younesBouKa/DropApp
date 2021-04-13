<template>
    <div style="height: 100%">
        <el-row type="flex" class="row-bg" justify="space-between" v-for="row in dataRows">
            <el-col :span="6" v-for="node in row">
                <div class="node-col">
                    <NodeCard :node="node"></NodeCard>
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
                nbr_cols_in_row: 4,
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {
            dataRows() {
                return _.chunk(this.data, this.nbr_cols_in_row);
            },

            /* ...mapGetters({
                 selectedNode:"getCurrentNode"
             }),*/
        },
        watch: {},
        mounted() {

        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            openFullScreen2() {
                const loading = this.$loading({
                    lock: true,
                    text: 'Loading',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
                setTimeout(() => {
                    loading.close();
                }, 2000);
            }
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
