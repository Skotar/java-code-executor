{
    "messages": [
<#list messages as message>
    "${message?js_string}"<#if message_has_next>,</#if>
</#list>
    ]
}