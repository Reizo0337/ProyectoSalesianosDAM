<script setup lang="ts">
    defineProps({
        headers: Array,
        data: Array
    })
</script>

<template>
    <table>
        <thead>
            <tr>
                <th v-for="header in headers" :key="header">{{ header }}</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="row in data" :key="row">
                <td v-for="cell in row" :key="cell">{{ cell }}</td>
            </tr>
        </tbody>
    </table>
</template>

<style scoped>
    table {
        width: 90%;
        border-collapse: collapse;
        margin: 0 auto;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        border-radius: 8px;
        overflow: hidden;
    }
    th, td {
        border: 1px solid #ddd;
        padding: 8px;
        text-align: left;
    }
    th {
        background-color: #f2f2f2;
        text-align: center;
    }
</style>