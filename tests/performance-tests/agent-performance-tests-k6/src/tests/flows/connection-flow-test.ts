import type { Options } from 'k6/options'
import { connectionFlow } from '../common'
import { defaultOptions } from '../../scenarios/default'
import { merge } from 'ts-deepmerge'

export const localOptions: Options = {
  thresholds: {
    'group_duration{group:::Issuer initiates connection with Holder}': [
      'avg<10000'
    ],
    'group_duration{group:::Holder accepts connection with Issuer}': [
      'avg<10000'
    ],
    'group_duration{group:::Issuer finalizes connection with Holder}': [
      'avg<10000'
    ],
    'group_duration{group:::Holder finalizes connection with Issuer}': [
      'avg<10000'
    ]
  }
}
export const options: Options = merge(localOptions, defaultOptions)

export default () => {
  connectionFlow()
}
